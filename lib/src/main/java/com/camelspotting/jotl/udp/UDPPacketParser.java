package com.camelspotting.jotl.udp;

import com.camelspotting.jotl.ClientsInfo;
import com.camelspotting.jotl.GRFRequest;
import com.camelspotting.jotl.parsing.ParseUtil;
import java.util.Arrays;
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

    private UDPPacketParser()
    {
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
     * @return
     */
//    public static ServerInfo parseServerInfo( byte[] data )
//    {
//    }
    /**
     * Based on OpenTTD source code:
     * <ul>
     * <li>Source file: src/network/core/udp.cpp</li>
     * <li>Packet sending code: SendNetworkGameInfo</li>
     * <li>Packet parsing code: ReceiveNetworkGameInfo</li>
     * </ul>
     *
     * @param data
     * @return
     */
    public static ClientsInfo parseClients( byte[] data )
    {
        data = Arrays.copyOfRange( data, 3, data.length );
        int i = 0;
        int version = data[i++];

        LOG.debug( "Parsing version {} packet, length {}: {}", version, data.length, Arrays.toString( data ) );

        GRFRequest[] grfs = null;
        if ( version >= 4 )
        {
            LOG.info( "Processing version 4 data." );
            grfs = new GRFRequest[ BitUtil.parse8BitNumber( data, i++ ) ];
            for ( int j = 0; j < grfs.length; j++ )
            {
                String id = Integer.toHexString( BitUtil.parse32BitNumber( data, i ) ).toUpperCase();
                i += 4;
                String md5 = "";
                for ( int k = 0; k < 16; k++ )
                {
                    md5 += BitUtil.parse8BitNumber( data, i++ );
                }
                md5 = md5.toUpperCase();
                grfs[j] = new GRFRequest( id, md5 );
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
        int[] gameVersion = ParseUtil.parseVersion( ParseUtil.parseString( data, i, length ).trim() );
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

        return new ClientsInfo( grfs, serverName, gameDate, startDate, maxNumberOfCompanies, numberOfActiveCompanies, maximumNumberOfSpectators, numberOfSpectatorsOn, maximumNumberOfClients, numberOfActiveClients, gameVersion, serverLang, passwordProtected, dedicated, tileset, mapHeight, mapWidth, mapName );
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
