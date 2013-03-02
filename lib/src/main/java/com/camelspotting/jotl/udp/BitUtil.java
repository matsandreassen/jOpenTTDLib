package com.camelspotting.jotl.udp;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BitUtil
{

    private static final Logger LOG = LoggerFactory.getLogger( BitUtil.class );

    private BitUtil()
    {
    }

    /**
     * Method for parsing 16 bytes.
     *
     * @param input where to find the bytes
     * @param offset where to start
     * @return the number.
     */
    public static long parse64BitNumber( byte[] input, int offset )
    {
        byte[] data = Arrays.copyOfRange( input, offset, offset + 8 );
        long[] unsigned = toUnsignedLongs( data );
        long value = unsigned[0];
        value += ( unsigned[1] << 8 );
        value += ( unsigned[2] << 16 );
        value += ( unsigned[3] << 24 );
        value += ( unsigned[4] << 32 );
        value += ( unsigned[5] << 40 );
        value += ( unsigned[6] << 48 );
        value += ( unsigned[7] << 56 );
        LOG.trace( "Parsing C signed long:" + Arrays.toString( data ) + " ==> " + value );
        return value;
    }

    /**
     * Method for parsing 4 bytes.
     *
     * @param input where to find the bytes
     * @param offset where to start
     * @return the number.
     */
    public static int parse32BitNumber( byte[] input, int offset )
    {
        byte[] data = Arrays.copyOfRange( input, offset, offset + 4 );
        int[] unsigned = toUnsignedIntegers( data );
        int value = ( unsigned[3] << 24 ) | ( unsigned[2] << 16 ) | ( unsigned[1] << 8 ) | unsigned[0];
        LOG.trace( "Parsing C signed long:" + Arrays.toString( data ) + " ==> " + value );
        return value;
    }

    /**
     * Method for parsing 2 bytes.
     *
     * @param input where to find the bytes
     * @param offset where to start
     * @return the number.
     */
    public static int parse16BitNumber( byte[] input, int offset )
    {
        byte[] data = Arrays.copyOfRange( input, offset, offset + 2 );
        int[] unsigned = toUnsignedIntegers( data );
        int value = ( unsigned[1] << 8 ) | unsigned[0];
        LOG.trace( "Parsing C signed long:" + Arrays.toString( data ) + " ==> " + value );
        return value;
    }

    /**
     * Method for parsing 1 byte.
     *
     * @param input where to find the bytes
     * @param offset where to start
     * @return the number.
     */
    public static int parse8BitNumber( byte[] input, int offset )
    {
        // Parse 1 byte
        return toUnsignedInt( input[offset] );
    }

    public static int toUnsignedInt( byte v )
    {
        int i = (int) v;
        return ~( i ^ 0xff ) & i;
    }

    public static int[] toUnsignedIntegers( byte[] b )
    {
        int[] A = new int[ b.length ];
        for ( int i = 0; i < b.length; i++ )
        {
            A[i] = toUnsignedInt( b[i] );
        }
        return A;
    }

    public static long[] toUnsignedLongs( byte[] b )
    {
        long[] A = new long[ b.length ];
        for ( int i = 0; i < b.length; i++ )
        {
            A[i] = toUnsignedInt( b[i] );
        }
        return A;
    }
}