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
     * Method for parsing the date to something readable.
     *
     * @param date the 32-bit date from OpenTTD
     * @return an array containing day, month, year
     */
    public static int[] parseDate( int date )
    {
        // Year determination in multiple steps to account for leap
        // years. First do the large steps, then the smaller ones.

        // There are 97 leap years in 400 years
        int year = 400 * ( date / ( 365 * 400 + 97 ) );
        int rem = date % ( 365 * 400 + 97 );

        if ( rem >= 365 * 100 + 25 )
        {
            // There are 25 leap years in the first 100 years after
            // every 400th year, as every 400th year is a leap year
            year += 100;
            rem -= 365 * 100 + 25;

            // There are 24 leap years in the next couple of 100 years
            year += 100 * ( rem / ( 365 * 100 + 24 ) );
            rem = ( rem % ( 365 * 100 + 24 ) );
        }

        if ( !IsLeapYear( year ) && rem >= 365 * 4 )
        {
            // The first 4 year of the century are not always a leap year
            year += 4;
            rem -= 365 * 4;
        }

        // There is 1 leap year every 4 years
        year += 4 * ( rem / ( 365 * 4 + 1 ) );
        rem = rem % ( 365 * 4 + 1 );

        // The last (max 3) years to account for; the first one
        // can be, but is not necessarily a leap year
        while ( rem >= ( IsLeapYear( year ) ? 366 : 365 ) )
        {
            rem -= IsLeapYear( year ) ? 366 : 365;
            year++;
        }

        /* Skip the 29th of February in non-leap years */
        if ( !IsLeapYear( year ) && rem >= ACCUM.MAR.getValue() - 1 )
        {
            rem++;
        }

        int x = monthFromYear[rem];
        int month = ( x >> 5 );
        int day = ( x & 0x1F );
        int[] A = new int[]
        {
            day, month, year
        };
        LOG.debug( "date({}) ==> {}", date, Arrays.toString( A ) );
        return A;
    }

    /**
     * Support method for determining whether a year is a leapyear.
     *
     * @param year the year to check
     * @return whether a year is a leapyear or not
     */
    private static boolean IsLeapYear( int year )
    {
        return year % 4 == 0 && ( year % 100 != 0 || year % 400 == 0 );
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

    /**
     * Support-method for creating the monthFromYear-array.
     *
     * @param a
     * @param b
     * @return
     * @see #parseDate(int date)
     */
    private static int M( int a, int b )
    {
        return ( a << 5 | b );
    }
    /**
     * Support array for deciphering what month a date has
     */
    private static int[] monthFromYear =
    {
        M( 0, 1 ), M( 0, 2 ), M( 0, 3 ), M( 0, 4 ), M( 0, 5 ), M( 0, 6 ), M( 0, 7 ), M( 0, 8 ), M( 0, 9 ), M( 0, 10 ), M( 0, 11 ), M( 0, 12 ), M( 0, 13 ), M( 0, 14 ), M( 0, 15 ), M( 0, 16 ), M( 0, 17 ), M( 0, 18 ), M( 0, 19 ), M( 0, 20 ), M( 0, 21 ), M( 0, 22 ), M( 0, 23 ), M( 0, 24 ), M( 0, 25 ), M( 0, 26 ), M( 0, 27 ), M( 0, 28 ), M( 0, 29 ), M( 0, 30 ), M( 0, 31 ),
        M( 1, 1 ), M( 1, 2 ), M( 1, 3 ), M( 1, 4 ), M( 1, 5 ), M( 1, 6 ), M( 1, 7 ), M( 1, 8 ), M( 1, 9 ), M( 1, 10 ), M( 1, 11 ), M( 1, 12 ), M( 1, 13 ), M( 1, 14 ), M( 1, 15 ), M( 1, 16 ), M( 1, 17 ), M( 1, 18 ), M( 1, 19 ), M( 1, 20 ), M( 1, 21 ), M( 1, 22 ), M( 1, 23 ), M( 1, 24 ), M( 1, 25 ), M( 1, 26 ), M( 1, 27 ), M( 1, 28 ), M( 1, 29 ),
        M( 2, 1 ), M( 2, 2 ), M( 2, 3 ), M( 2, 4 ), M( 2, 5 ), M( 2, 6 ), M( 2, 7 ), M( 2, 8 ), M( 2, 9 ), M( 2, 10 ), M( 2, 11 ), M( 2, 12 ), M( 2, 13 ), M( 2, 14 ), M( 2, 15 ), M( 2, 16 ), M( 2, 17 ), M( 2, 18 ), M( 2, 19 ), M( 2, 20 ), M( 2, 21 ), M( 2, 22 ), M( 2, 23 ), M( 2, 24 ), M( 2, 25 ), M( 2, 26 ), M( 2, 27 ), M( 2, 28 ), M( 2, 29 ), M( 2, 30 ), M( 2, 31 ),
        M( 3, 1 ), M( 3, 2 ), M( 3, 3 ), M( 3, 4 ), M( 3, 5 ), M( 3, 6 ), M( 3, 7 ), M( 3, 8 ), M( 3, 9 ), M( 3, 10 ), M( 3, 11 ), M( 3, 12 ), M( 3, 13 ), M( 3, 14 ), M( 3, 15 ), M( 3, 16 ), M( 3, 17 ), M( 3, 18 ), M( 3, 19 ), M( 3, 20 ), M( 3, 21 ), M( 3, 22 ), M( 3, 23 ), M( 3, 24 ), M( 3, 25 ), M( 3, 26 ), M( 3, 27 ), M( 3, 28 ), M( 3, 29 ), M( 3, 30 ),
        M( 4, 1 ), M( 4, 2 ), M( 4, 3 ), M( 4, 4 ), M( 4, 5 ), M( 4, 6 ), M( 4, 7 ), M( 4, 8 ), M( 4, 9 ), M( 4, 10 ), M( 4, 11 ), M( 4, 12 ), M( 4, 13 ), M( 4, 14 ), M( 4, 15 ), M( 4, 16 ), M( 4, 17 ), M( 4, 18 ), M( 4, 19 ), M( 4, 20 ), M( 4, 21 ), M( 4, 22 ), M( 4, 23 ), M( 4, 24 ), M( 4, 25 ), M( 4, 26 ), M( 4, 27 ), M( 4, 28 ), M( 4, 29 ), M( 4, 30 ), M( 4, 31 ),
        M( 5, 1 ), M( 5, 2 ), M( 5, 3 ), M( 5, 4 ), M( 5, 5 ), M( 5, 6 ), M( 5, 7 ), M( 5, 8 ), M( 5, 9 ), M( 5, 10 ), M( 5, 11 ), M( 5, 12 ), M( 5, 13 ), M( 5, 14 ), M( 5, 15 ), M( 5, 16 ), M( 5, 17 ), M( 5, 18 ), M( 5, 19 ), M( 5, 20 ), M( 5, 21 ), M( 5, 22 ), M( 5, 23 ), M( 5, 24 ), M( 5, 25 ), M( 5, 26 ), M( 5, 27 ), M( 5, 28 ), M( 5, 29 ), M( 5, 30 ),
        M( 6, 1 ), M( 6, 2 ), M( 6, 3 ), M( 6, 4 ), M( 6, 5 ), M( 6, 6 ), M( 6, 7 ), M( 6, 8 ), M( 6, 9 ), M( 6, 10 ), M( 6, 11 ), M( 6, 12 ), M( 6, 13 ), M( 6, 14 ), M( 6, 15 ), M( 6, 16 ), M( 6, 17 ), M( 6, 18 ), M( 6, 19 ), M( 6, 20 ), M( 6, 21 ), M( 6, 22 ), M( 6, 23 ), M( 6, 24 ), M( 6, 25 ), M( 6, 26 ), M( 6, 27 ), M( 6, 28 ), M( 6, 29 ), M( 6, 30 ), M( 6, 31 ),
        M( 7, 1 ), M( 7, 2 ), M( 7, 3 ), M( 7, 4 ), M( 7, 5 ), M( 7, 6 ), M( 7, 7 ), M( 7, 8 ), M( 7, 9 ), M( 7, 10 ), M( 7, 11 ), M( 7, 12 ), M( 7, 13 ), M( 7, 14 ), M( 7, 15 ), M( 7, 16 ), M( 7, 17 ), M( 7, 18 ), M( 7, 19 ), M( 7, 20 ), M( 7, 21 ), M( 7, 22 ), M( 7, 23 ), M( 7, 24 ), M( 7, 25 ), M( 7, 26 ), M( 7, 27 ), M( 7, 28 ), M( 7, 29 ), M( 7, 30 ), M( 7, 31 ),
        M( 8, 1 ), M( 8, 2 ), M( 8, 3 ), M( 8, 4 ), M( 8, 5 ), M( 8, 6 ), M( 8, 7 ), M( 8, 8 ), M( 8, 9 ), M( 8, 10 ), M( 8, 11 ), M( 8, 12 ), M( 8, 13 ), M( 8, 14 ), M( 8, 15 ), M( 8, 16 ), M( 8, 17 ), M( 8, 18 ), M( 8, 19 ), M( 8, 20 ), M( 8, 21 ), M( 8, 22 ), M( 8, 23 ), M( 8, 24 ), M( 8, 25 ), M( 8, 26 ), M( 8, 27 ), M( 8, 28 ), M( 8, 29 ), M( 8, 30 ),
        M( 9, 1 ), M( 9, 2 ), M( 9, 3 ), M( 9, 4 ), M( 9, 5 ), M( 9, 6 ), M( 9, 7 ), M( 9, 8 ), M( 9, 9 ), M( 9, 10 ), M( 9, 11 ), M( 9, 12 ), M( 9, 13 ), M( 9, 14 ), M( 9, 15 ), M( 9, 16 ), M( 9, 17 ), M( 9, 18 ), M( 9, 19 ), M( 9, 20 ), M( 9, 21 ), M( 9, 22 ), M( 9, 23 ), M( 9, 24 ), M( 9, 25 ), M( 9, 26 ), M( 9, 27 ), M( 9, 28 ), M( 9, 29 ), M( 9, 30 ), M( 9, 31 ),
        M( 10, 1 ), M( 10, 2 ), M( 10, 3 ), M( 10, 4 ), M( 10, 5 ), M( 10, 6 ), M( 10, 7 ), M( 10, 8 ), M( 10, 9 ), M( 10, 10 ), M( 10, 11 ), M( 10, 12 ), M( 10, 13 ), M( 10, 14 ), M( 10, 15 ), M( 10, 16 ), M( 10, 17 ), M( 10, 18 ), M( 10, 19 ), M( 10, 20 ), M( 10, 21 ), M( 10, 22 ), M( 10, 23 ), M( 10, 24 ), M( 10, 25 ), M( 10, 26 ), M( 10, 27 ), M( 10, 28 ), M( 10, 29 ), M( 10, 30 ),
        M( 11, 1 ), M( 11, 2 ), M( 11, 3 ), M( 11, 4 ), M( 11, 5 ), M( 11, 6 ), M( 11, 7 ), M( 11, 8 ), M( 11, 9 ), M( 11, 10 ), M( 11, 11 ), M( 11, 12 ), M( 11, 13 ), M( 11, 14 ), M( 11, 15 ), M( 11, 16 ), M( 11, 17 ), M( 11, 18 ), M( 11, 19 ), M( 11, 20 ), M( 11, 21 ), M( 11, 22 ), M( 11, 23 ), M( 11, 24 ), M( 11, 25 ), M( 11, 26 ), M( 11, 27 ), M( 11, 28 ), M( 11, 29 ), M( 11, 30 ), M( 11, 31 )
    };

    /**
     * Support enum for converting dates.
     *
     * @author Mats Andreassen
     * @version 1.0
     * @see #parseDate(int date)
     */
    private enum ACCUM
    {

        JAN( 0 ),
        FEB( JAN.value + 31 ),
        MAR( FEB.value + 29 ),
        APR( MAR.value + 31 ),
        MAY( APR.value + 30 ),
        JUN( MAY.value + 31 ),
        JUL( JUN.value + 30 ),
        AUG( JUL.value + 31 ),
        SEP( AUG.value + 31 ),
        OCT( SEP.value + 30 ),
        NOV( OCT.value + 31 ),
        DEC( NOV.value + 30 );
        private int value;

        /**
         * Simple constructor
         *
         * @param value the number of accumulated days
         */
        private ACCUM( int value )
        {
            this.value = value;
        }

        /**
         * Method for getting to the number of days accumulated up to this
         * month.
         *
         * @return the value
         */
        public int getValue()
        {
            return value;
        }
    }
}
