package com.camelspotting.jotl.udp;

import junitparams.JUnitParamsRunner;
import static junitparams.JUnitParamsRunner.$;
import junitparams.Parameters;
import org.joda.time.LocalDate;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;

/**
 *
 * @author Mats Andreassen
 */
@RunWith( value = JUnitParamsRunner.class )
public class DateUtilTest
{

    @Test
    @Parameters
    public void testConvertDateToYMD( LocalDate expectedDate, int date )
    {
        assertEquals( expectedDate, DateUtil.convertDateToYMD( date ) );
    }

    private Object[] parametersForTestConvertDateToYMD()
    {
        return $( $( new LocalDate( 1950, 3, 1 ), 712282 ) );
    }

    @Test
    @Parameters
    public void testConvertYMDToDate( int expectedOutput, LocalDate date )
    {
        assertEquals( expectedOutput, DateUtil.convertYMDToDate( date ) );
    }

    private Object[] parametersForTestConvertYMDToDate()
    {
        return $( $( 712282, new LocalDate( 1950, 3, 1 ) ) );
    }
}
