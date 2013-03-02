package com.camelspotting.jotl.domain;

import static junitparams.JUnitParamsRunner.*;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Mats Andreassen
 */
@RunWith( JUnitParamsRunner.class )
public class ServerTest
{

    @Test
    @Parameters
    public void testToString( String expected, Server instance )
    {
        assertEquals( expected, instance.toString() );
    }

    private Object[] parametersForTestToString()
    {
        return $(
                $( "Server: 178.33.34.239 @ 3379", new Server( "178.33.34.239", 3379, null ) ),
                $( "Server: openttd.org - 178.33.34.239 @ 3379", new Server( "openttd.org", "178.33.34.239", 3379, null ) ) );
    }
}
