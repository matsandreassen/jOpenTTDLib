package com.camelspotting.jotl.udp;

import com.camelspotting.jotl.ServerDetails;
import com.camelspotting.jotl.ClientsDetails;
import com.camelspotting.jotl.exceptions.JOTLException;
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
public class UDPPacketParserTest
{

    @Test
    @Parameters( method = "parameters" )
    public void testParseServerDetails( TestCase testCase ) throws JOTLException
    {
        ServerDetails clientsInfo = UDPPacketParser.parseServerDetails( testCase.getInput( PacketType.SERVER_RESPONSE ) );
        assertNotNull( clientsInfo );
    }

    private Object[] parameters()
    {
        return TestCase.values();
    }

    @Test
    @Parameters( method = "parameters" )
    public void testParseClientsDetails( TestCase testCase ) throws JOTLException
    {
        ClientsDetails serverInfo = UDPPacketParser.parseClientsDetails( testCase.getInput( PacketType.SERVER_DETAIL_INFO ) );
        assertNotNull( serverInfo );
    }

    public enum TestCase
    {

        G105( "1.0.5" );
        private final String version;

        private TestCase( String version )
        {
            this.version = version;
        }

        public String getVersion()
        {
            return version;
        }

        public byte[] getInput( PacketType pt )
        {
            if ( this == G105 )
            {
                switch ( pt )
                {
                    case SERVER_RESPONSE:
                        return new byte[]
                        {
                            47, 0, 1, 4, 0, 36, -34, 10, 0, 31, -34, 10, 0, 8, 1, 10, 115, 100, 0, 49, 46, 48, 46, 53, 0, 0, 0, 10, 1, 0, 82, 97, 110, 100, 111, 109, 32, 77, 97, 112, 0, 0, 1, 0, 1, 0, 0
                        };
                    case SERVER_DETAIL_INFO:
                        return new byte[]
                        {
                            66, 0, 3, 6, 1, 0, 85, 110, 110, 97, 109, 101, 100, 0, -98, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -96, -122, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
                        };
                }
            }

            throw new UnsupportedOperationException( String.format( "No input available for case %s and packet type %s.", this, pt ) );
        }
    }
}
