package com.camelspotting.jotl.udp;

import com.camelspotting.jotl.ClientsInfo;
import static junitparams.JUnitParamsRunner.*;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Mats Andreassen
 */
@RunWith( JUnitParamsRunner.class )
public class UDPPacketParserTest
{

    @Test
    @Parameters
    @Ignore
    public void testParseClients( int expectedVersion, byte[] reply )
    {
        ClientsInfo clientsInfo = UDPPacketParser.parseClients( reply );
        assertNotNull( clientsInfo );
    }

    private Object[] parametersForTestParseClients()
    {
        byte[] version6Reply = new byte[]
        {
            66, 0, 3, 6, 1, 0, 85, 110, 110, 97, 109, 101, 100, 0, -98, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -96, -122, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        };

        return $( $( 6, version6Reply ) );
    }
}
