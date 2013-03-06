package com.camelspotting.jotl.udp;

import com.camelspotting.jotl.domain.ServerDetails;
import com.camelspotting.jotl.NewGRF;
import com.camelspotting.jotl.domain.ClientsDetails;
import com.camelspotting.jotl.domain.ClientsDetailsV5;
import com.camelspotting.jotl.domain.Company;
import com.camelspotting.jotl.exceptions.JOTLException;
import com.camelspotting.jotl.parsing.ParseUtil;
import com.camelspotting.jotl.parsing.Station;
import com.camelspotting.jotl.parsing.Vehicle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Mats Andreassen
 */
public class UDPPacketParser
{

    private static final Logger LOG = LoggerFactory.getLogger( UDPPacketParser.class );
    /**
     * The length of the metadata segment
     */
    private static final int METADATA_LENGTH = 4;

    private UDPPacketParser()
    {
    }

    /**
     * Based on OpenTTD source code:
     * <ul>
     * <li>Source file: src/network/core/network_udp.cpp</li>
     * <li>Packet sending code: Receive_CLIENT_DETAIL_INFO</li>
     * <li>Packet parsing code: ?</li>
     * </ul>
     *
     * @param data
     * @see PacketType#CLIENT_DETAIL_INFO
     * @return
     */
    public static ClientsDetails parseClientsDetails( byte[] data ) throws JOTLException
    {
        PacketMetadata pm = verifyMetadata( data, PacketType.SERVER_DETAIL_INFO );
        int version = pm.getVersion();
        data = stripMetadata( data );

        int i = 0;
        int activePlayers = data[i++];

        if ( version < 4 )
        {
            throw new IllegalArgumentException( String.format( "Unsupported packet version: %d", version ) );
        }

        return parseVersion5( data, activePlayers, i );
    }

    private static ClientsDetailsV5 parseVersion5( byte[] data, int activePlayers, int i )
    {
        LOG.info( "Parsing version 5 info." );
        List<Company> companies = new ArrayList<Company>();
        for ( int j = 0; j < activePlayers; j++ )
        {
            int current = data[i++];
            int length = ParseUtil.locateNextZero( data, i );
            LOG.debug( "New company name seems to be {} characters long.", length );
            String compName = ParseUtil.parseString( data, i, length );
            i += length + 1;
            int inaugurated = BitUtil.parse32BitNumber( data, i );
            i += 4;
            long companyValue = BitUtil.parse64BitNumber( data, i );
            i += 8;
            long money = BitUtil.parse64BitNumber( data, i );
            i += 8;
            long income = BitUtil.parse64BitNumber( data, i );
            i += 8;
            int performance = BitUtil.parse16BitNumber( data, i );
            i += 2;
            boolean passwordProtected = ( data[i++] == 1 );


            Map<Vehicle, Integer> vehicleCountMap = new EnumMap<Vehicle, Integer>( Vehicle.class );
            // vehicle info
            for ( Vehicle v : Vehicle.values() )
            {
                vehicleCountMap.put( v, BitUtil.parse16BitNumber( data, i ) );
                i += 2;
            }

            // station info
            Map<Station, Integer> stationCountMap = new EnumMap<Station, Integer>( Station.class );
            for ( Station s : Station.values() )
            {
                stationCountMap.put( s, BitUtil.parse16BitNumber( data, i ) );
                i += 2;
            }

            Company com = new Company( current, compName, inaugurated, companyValue, money, income, performance, passwordProtected, vehicleCountMap, stationCountMap );
            LOG.debug( "Created {}.", com );
            LOG.debug( "{} has {} stations.", com.getCurrentId(), com.getNumberOfStations() );
            LOG.debug( "{} has {} vehicles.", com.getCurrentId(), com.getNumberOfVehicles() );
            companies.add( com );
        }

        return new ClientsDetailsV5( companies );
    }

    /**
     * Based on OpenTTD source code:
     * <ul>
     * <li>Source file: src/network/core/udp.cpp</li>
     * <li>Packet sending code: SendNetworkGameInfo</li>
     * <li>Packet parsing code: ReceiveNetworkGameInfo</li>
     * </ul>
     *
     * @param data
     * @see PacketType#SERVER_RESPONSE
     * @return
     */
    public static ServerDetails parseServerDetails( byte[] data ) throws JOTLException
    {
        PacketMetadata pm = verifyMetadata( data, PacketType.SERVER_RESPONSE );
        int version = pm.getVersion();
        data = stripMetadata( data );

        int i = 0;
        List<NewGRF> grfs = null;
        if ( version >= 4 )
        {
            LOG.info( "Processing version 4 data." );
            int grfCount = BitUtil.parse8BitNumber( data, i++ );
            grfs = new ArrayList<NewGRF>();
            for ( int j = 0; j < grfCount; j++ )
            {
                String id = Integer.toHexString( BitUtil.parse32BitNumber( data, i ) ).toUpperCase();
                i += 4;
                String md5 = "";
                for ( int k = 0; k < 16; k++ )
                {
                    md5 += Integer.toHexString( BitUtil.parse8BitNumber( data, i++ ) );
                }
                md5 = md5.toUpperCase();
                grfs.add( new NewGRF( id, md5 ) );
            }
        }

        LocalDate gameDate = null, startDate = null;
        if ( version >= 3 )
        {
            LOG.info( "Processing version 3 data." );
            int numberOfDays = BitUtil.parse32BitNumber( data, i );
            gameDate = DateUtil.convertDateToYMD( numberOfDays );
            LOG.debug( "Game date: {}", gameDate );
            i += 4;
            numberOfDays = BitUtil.parse32BitNumber( data, i );
            startDate = DateUtil.convertDateToYMD( numberOfDays );
            LOG.debug( "Start date: {}", startDate );
            i += 4;
        }

        int maxNumberOfCompanies = -1;
        int numberOfActiveCompanies = -1;
        int maximumNumberOfSpectators = -1;
        if ( version >= 2 )
        {
            LOG.info( "Processing version 2 data." );
            maxNumberOfCompanies = data[i++];
            numberOfActiveCompanies = data[i++];
            maximumNumberOfSpectators = data[i++];
        }

        LOG.info( "Processing version 1 data." );
        int length = ParseUtil.locateNextZero( data, i );
        LOG.debug( "Server name seems to be {} characters long.", length );
        String serverName = ParseUtil.parseString( data, i, length ).trim();
        i += length + 1;
        length = ParseUtil.locateNextZero( data, i );
        LOG.debug( "Revision seems to be {} characters long.", length );
        String gameVersion = ParseUtil.parseString( data, i, length );
        i += length + 1;
        byte serverLang = data[i++];
        boolean passwordProtected = data[i++] == 1;

        int maximumNumberOfClients = data[i++];
        int numberOfActiveClients = data[i++];
        int numberOfSpectatorsOn = data[i++];

        length = ParseUtil.locateNextZero( data, i );
        String mapName = ParseUtil.parseString( data, i, length ).trim();
        i += length + 1;
        int mapWidth = BitUtil.parse16BitNumber( data, i );
        i += 2;
        int mapHeight = BitUtil.parse16BitNumber( data, i );
        i += 2;
        byte tileset = data[i++];
        boolean dedicated = ( data[i++] == 1 );
        LOG.info( "Done parsing." );

        return new ServerDetails( grfs, serverName, gameDate, startDate, maxNumberOfCompanies, numberOfActiveCompanies, maximumNumberOfSpectators, numberOfSpectatorsOn, maximumNumberOfClients, numberOfActiveClients, gameVersion, serverLang, passwordProtected, dedicated, tileset, mapHeight, mapWidth, mapName );
    }

    /**
     * This method returns a "subarray", stripping the metadata
     *
     * @param input
     * @return
     * @see #METADATA_LENGTH
     */
    private static byte[] stripMetadata( byte[] input )
    {
        return Arrays.copyOfRange( input, METADATA_LENGTH, input.length );
    }

    /**
     * This method verifies that the packet is of the desired type.
     *
     * @param data the packet data
     * @param packetType the desired packet type
     * @return packet metadata
     * @throws JOTLException when the specified packet is of the wrong type
     */
    private static PacketMetadata verifyMetadata( byte[] data, PacketType packetType ) throws JOTLException
    {
        PacketMetadata pm = PacketMetadata.parseMetadata( data );
        if ( pm.getType() != packetType )
        {
            throw new JOTLException( String.format( "Expected packet type: %s. Received: %s.", packetType, pm.getType() ) );
        }

        LOG.debug( "Verified {}. Data: {}", pm, Arrays.toString( data ) );
        return pm;
    }
//
//    /**
//     * This method is for parsing the SERVER_NEWGRFS-packet.
//     *
//     * @param data the data buffer received
//     */
//    void parseGRFNames( byte[] data )
//    {
//        LOG.debug( "Parsing GRF names." );
//        int i = 0;
//        int oldCount = grfRequests.length;
//        int newCount = ParseUtil.parse8BitNumber( data, i++ );
//
//        // Expand the GRFRequest array
//        GRFRequest[] newArray = new GRFRequest[ oldCount + newCount ];
//        System.arraycopy( grfRequests, 0, newArray, 0, oldCount );
//        grfRequests = newArray;
//
//        for ( int j = oldCount; j < ( oldCount + newCount ); j++ )
//        {
//            String id = Integer.toHexString( ParseUtil.parse8BitNumber( data, i++ ) ).toUpperCase();
//            String md5 = "";
//            for ( int k = 0; k < 16; k++ )
//            {
//                md5 += ParseUtil.parse32BitNumber( data, i );
//                i += 4;
//            }
//            md5 = md5.toUpperCase();
//            int length = ParseUtil.locateNextZero( data, i );
//            String name = ParseUtil.parseString( data, i, length );
//            i += length + 1;
//            grfRequests[j] = new GRFRequest( id, md5, name );
//        }
//        LOG.debug( "Done parsing GRF names." );
//    }
}
