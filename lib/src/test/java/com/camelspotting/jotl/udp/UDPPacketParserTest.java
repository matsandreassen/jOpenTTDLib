package com.camelspotting.jotl.udp;

import com.camelspotting.jotl.domain.Client;
import com.camelspotting.jotl.domain.ClientsDetails;
import com.camelspotting.jotl.domain.ServerDetails;
import com.camelspotting.jotl.domain.ClientsDetailsV4;
import com.camelspotting.jotl.domain.ClientsDetailsV5;
import com.camelspotting.jotl.domain.Company;
import com.camelspotting.jotl.exceptions.JOTLException;
import com.camelspotting.jotl.parsing.Station;
import com.camelspotting.jotl.parsing.Vehicle;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.joda.time.LocalDate;
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
        ServerDetails expected = testCase.getServerDetails();
        ServerDetails actual = UDPPacketParser.parseServerDetails( testCase.getInput( PacketType.SERVER_RESPONSE ) );
        assertNotNull( actual );

        assertEquals( expected.getVersion(), actual.getVersion() );
        assertEquals( expected.getGameDate(), actual.getGameDate() );
        assertEquals( expected.getStartDate(), actual.getStartDate() );
        assertEquals( expected.getServerName(), actual.getServerName() );
        assertEquals( expected.getMapHeight(), actual.getMapHeight() );
        assertEquals( expected.getMapWidth(), actual.getMapWidth() );
        assertEquals( expected.getGraphicsCount(), actual.getGraphicsCount() );
        assertEquals( expected.getMaxNumberOfClients(), actual.getMaxNumberOfClients() );
        assertEquals( expected.getMaxNumberOfCompanies(), actual.getMaxNumberOfCompanies() );
        assertEquals( expected.getMaxNumberOfSpectators(), actual.getMaxNumberOfSpectators() );
        assertEquals( expected.getNumberOfActiveClients(), actual.getNumberOfActiveClients() );
        assertEquals( expected.getNumberOfActiveCompanies(), actual.getNumberOfActiveCompanies() );
        assertEquals( expected.getNumberOfActiveSpectators(), actual.getNumberOfActiveSpectators() );
        assertEquals( expected.getServerLanguage(), actual.getServerLanguage() );
        assertEquals( expected.getTileset(), actual.getTileset() );
        assertEquals( expected.isDedicated(), actual.isDedicated() );
        assertEquals( expected.isPasswordProtected(), actual.isPasswordProtected() );
    }

    private Object[] parameters()
    {
        return TestCase.values();
    }

    @Test
    @Parameters( method = "parameters" )
    public void testParseClientsDetails( TestCase testCase ) throws JOTLException
    {
        ClientsDetails expected = testCase.getClientsDetails();
        ClientsDetails actual = UDPPacketParser.parseClientsDetails( testCase.getInput( PacketType.SERVER_DETAIL_INFO ) );
        assertNotNull( actual );

        assertEquals( expected.getCompanies().size(), actual.getCompanies().size() );
        for ( int i = 0; i < expected.getCompanies().size(); i++ )
        {
            Company expectedCom = expected.getCompanies().get( i );
            Company actualCom = actual.getCompanies().get( i );

            assertEquals( expectedCom.getBalance(), actualCom.getBalance() );
            assertEquals( expectedCom.getCompanyName(), actualCom.getCompanyName() );
            assertEquals( expectedCom.getCompanyValue(), actualCom.getCompanyValue() );
            assertEquals( expectedCom.getCurrentId(), actualCom.getCurrentId() );
            assertEquals( expectedCom.getIncome(), actualCom.getIncome() );
            assertEquals( expectedCom.getInaugerationYear(), actualCom.getInaugerationYear() );
            assertEquals( expectedCom.getRating(), actualCom.getRating() );

            for ( Vehicle v : Vehicle.values() )
            {
                assertEquals( expectedCom.getNumberOfVehicles().get( v ), actualCom.getNumberOfVehicles().get( v ) );
            }

            for ( Station s : Station.values() )
            {
                assertEquals( expectedCom.getNumberOfStations().get( s ), actualCom.getNumberOfStations().get( s ) );
            }
        }

        if ( expected instanceof ClientsDetailsV4 )
        {
            assertEquals( expected.getClients().size(), actual.getClients().size() );
            for ( int i = 0; i < expected.getClients().size(); i++ )
            {
                Client expectedClient = expected.getClients().get( i );
                Client actualClient = actual.getClients().get( i );

                assertEquals( expectedClient.getJoinDate(), actualClient.getJoinDate() );
                assertEquals( expectedClient.getName(), actualClient.getName() );
                assertEquals( expectedClient.getUniqueId(), actualClient.getUniqueId() );
            }
        }
    }

    public enum TestCase
    {

        G105;

        private TestCase()
        {
        }

        public ClientsDetails getClientsDetails()
        {
            switch ( this )
            {
                case G105:
                    EnumMap<Vehicle, Integer> vehicleMap = new EnumMap<Vehicle, Integer>( Vehicle.class );
                    for ( Vehicle vehicle : Vehicle.values() )
                    {
                        vehicleMap.put( vehicle, 0 );
                    }
                    EnumMap<Station, Integer> stationMap = new EnumMap<Station, Integer>( Station.class );
                    for ( Station station : Station.values() )
                    {
                        stationMap.put( station, 0 );
                    }

                    List<Company> companies = new ArrayList<Company>();
                    Company com = new Company( 0, "Unnamed", 1950, 0, 100000, 0, 0, false, vehicleMap, stationMap );
                    companies.add( com );
                    return new ClientsDetailsV5( companies );
                default:
                    throw new UnsupportedOperationException( String.format( "Unsupported case: %s", this ) );
            }
        }

        public ServerDetails getServerDetails()
        {
            switch ( this )
            {
                case G105:
                    return new ServerDetails( null, "sd", new LocalDate( 1950, 1, 6 ), new LocalDate( 1950, 1, 1 ), 8, 1, 10, 0, 10, 1, "1.0.5", 0, false, false, 0, 256, 256, "Random Map" );
                default:
                    throw new UnsupportedOperationException( String.format( "Unsupported case: %s", this ) );
            }
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
