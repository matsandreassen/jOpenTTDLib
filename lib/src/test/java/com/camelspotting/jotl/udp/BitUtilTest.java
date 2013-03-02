package com.camelspotting.jotl.udp;

import junitparams.JUnitParamsRunner;
import static junitparams.JUnitParamsRunner.$;
import junitparams.Parameters;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;

/**
 *
 * @author Mats Andreassen
 */
@RunWith( value = JUnitParamsRunner.class )
public class BitUtilTest
{

    @Test
    @Parameters
    public void testToUnsignedInt( byte b, int expected )
    {
        assertEquals( expected, BitUtil.toUnsignedInt( b ) );
    }

    private Object[] parametersForTestToUnsignedInt()
    {
        return $( $( (byte) -66, 190 ), $( (byte) -77, 179 ), $( (byte) 47, 47 ), $( (byte) 0, 0 ) );
    }

    @Test
    @Parameters
    public void testParse32BitNumber( int expected, byte[] array )
    {
        assertEquals( expected, BitUtil.parse32BitNumber( array, 0 ) );
    }

    private Object[] parametersForTestParse32BitNumber()
    {
        return $( $( 3126206, new byte[]
        {
            -66, -77, 47, 0
        } ), $( -35815, new byte[]
        {
            25, 116, -1, -1
        } ), $( -142769, new byte[]
        {
            79, -46, -3, -1
        } ) );
    }

    @Test
    @Parameters
    public void testToUnsignedIntegers( int[] expected, byte[] array )
    {
        assertArrayEquals( expected, BitUtil.toUnsignedIntegers( array ) );
    }

    public Object[] parametersForTestToUnsignedIntegers()
    {
        int[] expected = new int[]
        {
            190, 179, 47, 0
        };
        byte[] array = new byte[]
        {
            -66, -77, 47, 0
        };
        return new Object[]
        {
            new Object[]
            {
                expected, array
            }
        };
    }
}