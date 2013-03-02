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
package com.camelspotting.jotl.udp;

import com.camelspotting.jotl.ClientsInfo;
import com.camelspotting.jotl.GameQuerier;
import com.camelspotting.jotl.ServerInfo;
import com.camelspotting.jotl.domain.Game;
import com.camelspotting.jotl.exceptions.JOTLException;
import com.camelspotting.jotl.parsing.ParseUtil;
import com.camelspotting.jotl.domain.Server;
import com.camelspotting.jotl.exceptions.IllegalHostException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
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
public final class UDPGameQuerier implements GameQuerier
{

    /**
     * The logger object for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger( UDPGameQuerier.class );
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
     * Main constructor for class.
     *
     * @param host the hostname (openttd.someserver.com) or IPv4 to contact (ex:
     * 127.0.0.1)
     * @param fromPort the port to use
     * @param destPort the port to contact
     * @throws com.camelspotting.openttd.JOTLException
     */
    public UDPGameQuerier( String host, int fromPort, int destPort ) throws IllegalHostException
    {
        this.fromPort = fromPort;
        this.server = ParseUtil.parseHost( host, destPort );
    }

    @Override
    public ClientsInfo getClientsInfo() throws JOTLException
    {
        DatagramSocket socket = null;
        try
        {
            socket = bind();
            sendPacket( socket, PacketType.CLIENT_DETAIL_INFO );
            byte[] reply = recieve( socket );
            PacketType type = PacketType.fromInt( reply[2] );

            if ( type != PacketType.SERVER_DETAIL_INFO )
            {
                throw new JOTLException( String.format( "Expected packet type: %s. Received: %s.", PacketType.CLIENT_DETAIL_INFO, type ) );
            }

            return UDPPacketParser.parseClients( reply );
        }
        catch ( IOException ex )
        {
            throw new JOTLException( ex );
        }
        finally
        {
            unbind( socket );
        }
    }

    @Override
    public ServerInfo getServerInfo() throws JOTLException
    {
        DatagramSocket socket = null;
        try
        {
            socket = bind();
            sendPacket( socket, PacketType.CLIENT_FIND_SERVER );
            byte[] reply = recieve( socket );
            PacketType type = PacketType.fromInt( reply[2] );

            if ( type != PacketType.SERVER_RESPONSE )
            {
                throw new JOTLException( String.format( "Expected packet type: %s. Received: %s.", PacketType.CLIENT_DETAIL_INFO, type ) );
            }

            return UDPPacketParser.parseServerInfo( reply );
        }
        catch ( IOException ex )
        {
            throw new JOTLException( ex );
        }
        finally
        {
            unbind( socket );
        }
    }

    @Override
    public Game getAllInformation() throws JOTLException
    {
        return new Game( getClientsInfo(), getServerInfo() );
    }

    @Override
    public Server getServer()
    {
        return server;
    }

    private void sendPacket( DatagramSocket socket, PacketType pt ) throws IOException, JOTLException
    {
        if ( pt.getPacketOrigin() != PacketType.PacketOrigin.CLIENT )
        {
            throw new JOTLException( String.format( "Can only send packets which originate from client and not: %s", pt ) );
        }
        DatagramPacket querypacket = pt.createPacket( server.getAddress(), server.getPort() );
        socket.send( querypacket );
        LOG.debug( "Packet of type {} sent.", pt );
    }

    private DatagramSocket bind() throws SocketException
    {
        DatagramSocket socket = new DatagramSocket( fromPort );
        socket.setSoTimeout( 5000 );
        return socket;
    }

    private void unbind( DatagramSocket socket )
    {
        if ( socket == null )
        {
            LOG.debug( "No socket to unbind!" );
            return;
        }

        socket.close();
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
    private byte[] recieve( DatagramSocket socket ) throws IOException, SocketException
    {
        byte[] reply = new byte[ maxPacketSize ];
        DatagramPacket recieved = new DatagramPacket( reply, maxPacketSize );
        socket.receive( recieved ); // This call blocks
        byte[] data = trimPacket( recieved.getData(), recieved.getLength() );

        PacketType type = PacketType.fromInt( data[2] );

        StringBuilder sb = new StringBuilder( "Recieved packet!" );
        sb.append( "\n\tPacket Type: " ).append( type );
        sb.append( "\n\tOpenTTD UDP-query Version: " ).append( data[3] );
        sb.append( "\n\tMessage length: " ).append( data.length );
        sb.append( "\n\tMessage: " ).append( Arrays.toString( data ) );
        LOG.debug( sb.toString() );
        return data;
    }
}
