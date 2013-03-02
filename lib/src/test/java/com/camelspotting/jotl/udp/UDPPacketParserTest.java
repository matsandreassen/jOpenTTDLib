package com.camelspotting.jotl.udp;

import com.camelspotting.jotl.ClientsInfo;
import com.camelspotting.jotl.ServerInfo;
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

    @Test
    @Parameters
    @Ignore
    public void testParseServerInfo( int expectedVersion, byte[] reply )
    {
        ServerInfo serverInfo = UDPPacketParser.parseServerInfo( reply );
        assertNotNull( serverInfo );
    }

    private Object[] parametersForTestParseServerInfo()
    {
        byte[] version4Reply = new byte[]
        {
            47, 0, 1, 4, 0, 36, -34, 10, 0, 31, -34, 10, 0, 8, 1, 10, 115, 100, 0, 49, 46, 48, 46, 53, 0, 0, 0, 10, 1, 0, 82, 97, 110, 100, 111, 109, 32, 77, 97, 112, 0, 0, 1, 0, 1, 0, 0
        };

        return $( $( 4, version4Reply ) );
    }
}
