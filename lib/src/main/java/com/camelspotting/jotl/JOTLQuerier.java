/*
 * Copyright (c) 2007 Mats Andreassen <matsa@pvv.ntnu.no>
 * Copyright (c) 2007 Eivind Brandth Smedseng <eivbsmed@gmail.com>
 * All rights reserved.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.camelspotting.jotl;

import com.camelspotting.jotl.domain.Server;
import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is one of the primary access points for any user of the library. For
 * each server you want to gather information from an object of this class must
 * be instantiated. It supports both DNS and IPv4-addresses. If you use any of
 * the constructors that have the option of not querying the server immidiately
 * the query-metods may be used to gather information post-constructing the
 * object.
 *
 * @author Eivind Brandth Smedseng
 * @author Mats Andreassen
 * @version 1.0
 * @see #query(SendablePacketType pt)
 * @see #queryAll()
 */
public final class JOTLQuerier implements Comparable<JOTLQuerier>
{

    /**
     * The logger object for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger( JOTLQuerier.class );
    /**
     * Maximum packet size for receiving
     */
    private static final int maxPacketSize = 1000;
    /**
     * This is the server
     */
    private Server server;
    /**
     * The local port
     */
    private int fromPort;
    /**
     * The remote port
     */
    private int destPort;
    /**
     * The socket to send and receive messages with
     */
    private DatagramSocket socket;
    /**
     * One of the objects that will contain gathered information
     */
    private Clients serverResponseInfo;
    /**
     * One of the objects that will contain gathered information
     */
    private ServerInfo serverDetailedInfo;
    /**
     * Whether or not to print debug messages.
     */
    public static boolean debug = false;
    /**
     * boolean for hindering data of being overwritten
     */
    private boolean writable = true;
    /**
     * When was this class instantiated?
     */
    private long timeStamp;

    /**
     * This is a method for checking whether there is a server listening at this
     * address and at the remote port.
     *
     * @param server the address to look up
     * @param localPort the local port to use
     * @param destPort the remote port to use
     * @param timeout number of milliseconds to wait for reply
     * @return the byte[] array containing the data for the
     * CLIENT_FIND_SERVER-packet
     * @see SendablePacketType
     */
    static byte[] testConfiguration( Server server, int localPort, int destPort, int timeout )
    {
        // Send a test packet to see if there is an OpenTTD-server at location
        DatagramSocket socket = null;
        try
        {
            socket = new DatagramSocket( localPort );
            socket.setSoTimeout( timeout );
            LOG.info( "Pinging OpenTTD server at {}.", server.getIpAddress() );
            socket.send( PacketType.CLIENT_FIND_SERVER.createPacket( server.getAddress(), destPort ) );
            DatagramPacket dp = new DatagramPacket( new byte[ maxPacketSize ], maxPacketSize );
            socket.receive( dp );
            LOG.info( "We got a reply! There is most definitely something there." );
            return trimPacket( dp.getData(), dp.getLength() );
        }
        catch ( BindException bex )
        {
            LOG.info( "This local port '{}' was already in use.", localPort );
            return null;
        }
        catch ( SocketException sex )
        {
            LOG.info( "We timed out. Couldn't locate any OpenTTD server here." );
            return null;
        }
        catch ( IOException ioe )
        {
            LOG.info( "We could not even try to reach the server. What's wrong?", ioe );
            return null;
        }
        finally
        {
            if ( socket != null )
            {
                socket.close();
            }
        }
    }

    /**
     * This is a convience constructor only accessible inside the package to
     * facilitate easier use from ServerHandler.
     *
     * @param host the hostname (openttd.someserver.com) or IPv4 to contact (ex:
     * 127.0.0.1)
     * @param fromPort the port to use
     * @param destPort the port to contact
     * @param queryInfo whether to query server immidiately
     * @see ServerHandler
     */
    JOTLQuerier( Server server, int fromPort, int destPort, boolean queryInfo ) throws JOTLException
    {
        this.timeStamp = System.currentTimeMillis();
        this.server = server;
        this.fromPort = fromPort;
        this.destPort = destPort;
        if ( queryInfo )
        {
            queryAll();
        }
    }

    /**
     * Main constructor for class.
     *
     * @param host the hostname (openttd.someserver.com) or IPv4 to contact (ex:
     * 127.0.0.1)
     * @param fromPort the port to use
     * @param destPort the port to contact
     * @param queryInfo whether to query server immidiately
     * @throws com.camelspotting.openttd.JOTLException
     */
    public JOTLQuerier( String host, int fromPort, int destPort, boolean queryInfo ) throws JOTLException
    {
        this.fromPort = fromPort;
        this.destPort = destPort;
        try
        {
            this.server = Parser.parseHost( host );
            byte[] data = testConfiguration( server, fromPort, destPort, 2000 );
            if ( data == null )
            {
                throw new JOTLException( "No server replies on this address and port." );
            }
            else if ( queryInfo )
            {
                recieve( data );
                queryRest( PacketType.CLIENT_FIND_SERVER );
            }
        }
        catch ( UnknownHostException ex )
        {
            throw new JOTLException( "The host could not be reached.", ex );
        }
        catch ( IOException ioe )
        {
            throw new JOTLException( "IO exception when trying to reach the server..", ioe );
        }
    }

    /**
     * Constructor with default values of using local port 2222 and remote port
     * 3979.
     *
     * @param host the hostname (openttd.someserver.com) or IPv4 to contact (ex:
     * 127.0.0.1)
     * @param queryInfo whether to query server immidiately
     * @throws com.camelspotting.openttd.JOTLException
     */
    public JOTLQuerier( String host, boolean queryInfo ) throws JOTLException
    {
        this( host, 2222, 3979, queryInfo );
    }

    /**
     * Constructor with default values of using local port 2222 and remote port
     * 3979. Will query the server immidiately.
     *
     * @param host the hostname (openttd.someserver.com) or IPv4 to contact (ex:
     * 127.0.0.1)
     * @throws com.camelspotting.openttd.JOTLException
     */
    public JOTLQuerier( String host ) throws JOTLException
    {
        this( host, 2222, 3979, true );
    }

    /**
     * This method is for making the program print out debug messages to stdout.
     *
     * @param on whether or not to print messages
     */
    public void setDebugMode( boolean on )
    {
        debug = on;
    }

    /**
     * This method constructs the grfs request packet. This is done here instead
     * of in the enum since it requires dynamic coding.
     *
     * @return a {@link DatagramPacket} ready for sending
     */
    private DatagramPacket createNewGRFSPacket()
    {
        DatagramPacket querypacket = null;
        //TODO: Bruk informasjonen lagret i ClientsInfo.
        return querypacket;
    }

    /**
     * Method for querying the server for information.
     *
     * @param pt the type of {@link SendablePacketType} to send
     * @throws com.camelspotting.openttd.JOTLException
     */
    public void query( PacketType pt ) throws JOTLException
    {
        LOG.trace( "query(packet={})", pt );
        if ( !writable )
        {
            throw new JOTLException( "The program has disallowed this operation at this point." );
        }

        try
        {
            if ( socket == null || socket.isClosed() )
            {
                socket = new DatagramSocket( fromPort );
                socket.setSoTimeout( 5000 );
            }
            DatagramPacket querypacket;
            /*if (pt == SendablePacketType.CLIENT_GET_NEWGRFS) {
             // This packet type is dependant on the FIND_SERVER packet
             if (serverResponseInfo == null) {
             query(SendablePacketType.CLIENT_FIND_SERVER);
             }
             querypacket = createNewGRFSPacket();
             } else {*/
            querypacket = pt.createPacket( server.getAddress(), destPort );
            //}
            socket.send( querypacket );
            LOG.debug( "Packet of type {} sent.", pt.toString() );
            recieve( null );
        }
        catch ( IOException ioe )
        {
            boolean closed = false;
            if ( socket != null )
            {
                try
                {
                    socket.close();
                    closed = true;
                }
                catch ( Exception ex )
                {
                    LOG.error( "Could not close socket.", ex );
                }
            }
            throw new JOTLException( ioe.getMessage() + ( closed ? " Action taken: Socket closed." : "" ), ioe );
        }
    }

    private static byte[] trimPacket( byte[] data, int actualLength )
    {
        byte[] A = new byte[ actualLength ];
        System.arraycopy( data, 0, A, 0, actualLength );
        return A;
    }

    /**
     * Private method for recieving replies from the server.
     *
     * @throws com.camelspotting.openttd.JOTLException
     */
    private void recieve( byte[] data ) throws IOException, SocketException
    {
        if ( data == null )
        {
            byte[] reply = new byte[ maxPacketSize ];
            DatagramPacket recieved = new DatagramPacket( reply, maxPacketSize );
            socket.receive( recieved ); //Blokkerende metodekall
            socket.close();
            data = trimPacket( recieved.getData(), recieved.getLength() );
        }

        PacketType type = PacketType.fromInt( data[2] );

        StringBuilder sb = new StringBuilder( "Recieved packet!" );
        sb.append( "\n\tPacket Type " ).append( type ).append( ":" );
        sb.append( "\n\tOpenTTD UDP-query Version: " ).append( data[3] );
        sb.append( "\n\tMessage length: " ).append( data.length ).append( "." );
        sb.append( "\n\tMessage: " ).append( Arrays.toString( data ) );
        LOG.debug( sb.toString() );

        switch ( type )
        {
            case SERVER_RESPONSE:
                LOG.info( "Parsing server response information." );
                this.serverResponseInfo = new Clients( data );
                LOG.info( "Server response information parsed." );
                break;
            case SERVER_DETAIL_INFO:
                LOG.info( "Parsing server detailed information." );
                this.serverDetailedInfo = new ServerInfo( data );
                LOG.info( "Server detailed information parsed." );
                break;
            case SERVER_NEWGRFS:
                LOG.info( "Parsing newGRFs information." );
                serverResponseInfo.parseGRFNames( data );
                LOG.info( "NewGRFs information parsed." );
                break;
            default:
                throw new UnsupportedOperationException( String.format( "Unsupported packet type received: %s", type ) );
        }
    }

    /**
     * This package-restricted method is so that the program may render this
     * object unable to do updates. This can only be done by any program written
     * by the authors.
     */
    void makeUnupdatable()
    {
        writable = false;
    }

    /**
     * Convenience method for updating all information.
     *
     * @throws com.camelspotting.openttd.JOTLException
     * @see #query(SendablePacketType pt)
     */
    public void queryAll() throws JOTLException
    {
        if ( !writable )
        {
            throw new JOTLException( "The program has disallowed this operation at this point." );
        }

        for ( PacketType spt : PacketType.values() )
        {
            query( spt );
        }
    }

    private void queryRest( PacketType exceptThisOne ) throws JOTLException
    {
        for ( PacketType spt : PacketType.values() )
        {
            if ( spt != exceptThisOne )
            {
                query( spt );
            }
        }
    }

    /**
     * Method for accessing server info.
     *
     * @return the current {@link ClientsInfo} object or null if no info has
     * been collected
     */
    public Clients getServerResponseInfo()
    {
        return serverResponseInfo;
    }

    /**
     * Method for accessing client info.
     *
     * @return the current {@link ServerInfo} object or null if no info has been
     * collected
     */
    public ServerInfo getServerDetailedInfo()
    {
        return serverDetailedInfo;
    }

    /**
     * This method compares when the objects were instantiated.
     *
     * @param o the object to compare with
     * @return 1 if this is newer, -1 if this is older, or 0 if equal
     */
    @Override
    public int compareTo( JOTLQuerier o )
    {
        long diff = o.timeStamp - this.timeStamp;
        if ( diff > 0 )
        {
            return 1;
        }
        else if ( diff < 0 )
        {
            return -1;
        }
        else
        {
            return 0;
        }
    }

    /**
     * Convenience method for archiving gathered information.
     *
     * @return a {@link Game} object containing the gathered information
     */
    public Game toGame()
    {
        return new Game( serverResponseInfo, serverDetailedInfo );
    }

    /**
     * Method for getting a textual representation of this object. Very useful
     * for debugging.
     *
     * @return a {@link String} containing all data.
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder( "JOTLQuerier: \n" );
        sb.append( serverResponseInfo.toString() );
        sb.append( serverDetailedInfo.toString() );
        return sb.toString();
    }
}
