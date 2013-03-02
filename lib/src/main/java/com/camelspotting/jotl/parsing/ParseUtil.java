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
package com.camelspotting.jotl.parsing;

import com.camelspotting.jotl.domain.Server;
import com.camelspotting.jotl.exceptions.IllegalHostException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a class of static parse support methods, so no methods are visible to
 * applications using jOpenTTDLib.
 *
 * @author Eivind Brandth Smedseng
 * @author Mats Andreassen
 * @version 1.0
 */
public final class ParseUtil
{

    /**
     * The logger object for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger( ParseUtil.class );
    /**
     * The pattern for matching an IPv4 address.
     */
    private static String ipv4Pattern = "[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}";
    /**
     * OpenTTD doesn't support ipv6 yet
     */
    private static String ipv6Pattern;

    /**
     * This constructor is only here to prevent this class from being
     * instantiated.
     */
    private ParseUtil()
    {
    }

    /**
     * Method for parsing the the server version into components.
     *
     * @param version the version to parse
     * @return e.g.: { 0, 5, 3}
     */
    public static int[] parseVersion( String version )
    {
        String[] A = version.split( "\\." );
        int[] B = new int[ A.length ];
        for ( int i = 0; i < A.length; i++ )
        {
            B[i] = Integer.valueOf( A[i] );
        }
        return B;
    }

    /**
     * Finds first zero from offset.
     *
     * @param data the array to check
     * @param offset where to start
     * @return the length to the location
     */
    public static int locateNextZero( byte[] data, int offset )
    {
        int length = 0;
        while ( data[offset++] != 0 )
        {
            length++;
        }
        return length;
    }

    /**
     * Method for parsing contents of offset byte array to offset
     * {@link String}.
     *
     * @param data the byte array to read from
     * @param offset where to start
     * @param length where to finish
     * @return the finished {@link String}
     */
    public static String parseString( byte[] data, int offset, int length )
    {
        return new String( data, offset, length );
    }

    /**
     * This method checks if host is an IP-address via regex and parses it or if
     * not, does a DNS look up on it.
     *
     * @param host the hostname or IP
     * @return an {@link InetAddress}-object
     * @throws java.net.UnknownHostException
     */
    public static Server parseHost( String host, int port ) throws IllegalHostException
    {
        try
        {
            InetAddress address;
            String ip;
            String hostname = null;
            if ( Pattern.matches( ipv4Pattern, host ) )
            {
                ip = host;

                address = parseIPv4( host );
            }
            else
            {   // Try to resolve through DNS
                address = InetAddress.getByName( host );
                ip = address.getHostAddress();
                LOG.debug( "{} was resolved to {}", host, ip );
                return new Server( host, address.getHostAddress(), port, address );
            }
            return new Server( hostname, ip, port, address );
        }
        catch ( UnknownHostException ex )
        {
            Server s = new Server( host, port, null );
            throw new IllegalHostException( s, ex );
        }
    }

    /**
     * Conveninence method for parsing an version 4 IP-address.
     *
     * @param host the IP to parse
     * @return an {@link InetAddress}-object
     * @throws java.net.UnknownHostException
     */
    private static InetAddress parseIPv4( String host ) throws UnknownHostException
    {
        String[] S = host.split( "\\." );
        byte[] A = new byte[ 4 ];
        for ( int i = 0; i < S.length; i++ )
        {
            A[i] = (byte) (int) Integer.valueOf( S[i] );
        }
        LOG.debug( "Parsed ipv4 to: {}", Arrays.toString( A ) );
        return InetAddress.getByAddress( new byte[]
        {
            A[0], A[1], A[2], A[3]
        } );
    }
}
