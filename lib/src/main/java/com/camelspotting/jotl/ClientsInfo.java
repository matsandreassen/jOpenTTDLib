/*
 * Copyright (c) 2007 Mats Andreassen <matsa@pvv.ntnu.no>
 * Copyright (c) 2007 Eivind Brandth Smedseng <eivbsmed@gmail.com>
 * All rights reserved.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.camelspotting.jotl;

import com.camelspotting.jotl.parsing.ParseUtil;
import java.util.Arrays;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is for parsing and holding the information from the server
 * response package.
 *
 * @author Eivind Brandth Smedseng
 * @author Mats Andreassen
 * @version 1.0
 * @see SendablePacketType
 */
public final class ClientsInfo
{

    /**
     * The logger object for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger( ClientsInfo.class );
    /**
     * The graphics requests
     */
    private GRFRequest[] grfRequests;
    /**
     * The server's name
     */
    private String serverName;
    /**
     * Holds the current game date
     */
    private int[] gameDate;
    /**
     * Holds the game's start date
     */
    private int[] startDate;
    /**
     * The maximum allowed companies
     */
    private int companies_max;
    /**
     * The number of companies on at the moment
     */
    private int companies_on;
    /**
     * The maximum allowed spectators
     */
    private int spectators_max;
    /**
     * The number of spectators on at the moment
     */
    private int spectators_on;
    /**
     * The maximum allowed clients
     */
    private int clients_max;
    /**
     * The number of clients on at the moment
     */
    private int clients_on;
    /**
     * The game version
     */
    private int[] revision;
    /**
     * The language the server is running
     */
    private int serverLang;
    /**
     * Whether the server is password protected
     */
    private boolean password_protected;
    /**
     * Whether the server running dedicated
     */
    private boolean dedicated;
    /**
     * What tileset is in play
     */
    private int tileset;
    /**
     * The current map's height
     */
    private int map_height;
    /**
     * The current map's width
     */
    private int map_width;
    /**
     * The current map's name
     */
    private String map_name;

    /**
     * The constructor parses the buffer for all interesting data that is
     * contained within.
     *
     * @param data the buffer
     */
    public ClientsInfo( byte[] data )
    {
        int i = 3;
        int version = data[i++];

        switch ( version )
        {
            case 4:
                LOG.info( "Processing version 4 data." );
                this.grfRequests = new GRFRequest[ data[i++] ];
                for ( int j = 0; j < grfRequests.length; j++ )
                {
                    String id = Integer.toHexString( ParseUtil.parse32BitNumber( data, i ) ).toUpperCase();
                    i += 4;
                    String md5 = "";
                    for ( int k = 0; k < 16; k++ )
                    {
                        md5 += ParseUtil.parse8BitNumber( data, i++ );
                    }
                    md5 = md5.toUpperCase();
                    grfRequests[j] = new GRFRequest( id, md5 );
                }
            case 3:
                LOG.info( "Processing version 3 data." );
                this.gameDate = ParseUtil.parseDate( ParseUtil.parse32BitNumber( data, i ) );
                LOG.debug( "Game date: {}" , Arrays.toString( gameDate ) );
                i += 4;
                this.startDate = ParseUtil.parseDate( ParseUtil.parse32BitNumber( data, i ) );
                LOG.debug( "Start date: {}", Arrays.toString( startDate ) );
                i += 4;
            case 2:
                LOG.info( "Processing version 2 data." );
                this.companies_max = data[i++];
                this.companies_on = data[i++];
                this.spectators_max = data[i++];
            case 1:
                LOG.info( "Processing version 1 data." );
                int length = ParseUtil.locateNextZero( data, i );
                LOG.debug( "Server name seems to be {} characters long.", length );
                this.serverName = ParseUtil.parseString( data, i, length ).trim();
                i += length + 1;
                length = ParseUtil.locateNextZero( data, i );
                LOG.debug( "Revision seems to be {} characters long.", length );
                this.revision = ParseUtil.parseVersion( ParseUtil.parseString( data, i, length ).trim() );
                i += length + 1;
                this.serverLang = data[i++];
                this.password_protected = data[i++] == 1;
                this.clients_max = data[i++];
                this.clients_on = data[i++];
                this.spectators_on = data[i++];
                length = ParseUtil.locateNextZero( data, i );
                this.map_name = ParseUtil.parseString( data, i, length ).trim();
                i += length + 1;
                this.map_width = ParseUtil.parse16BitNumber( data, i );
                i += 2;
                this.map_height = ParseUtil.parse16BitNumber( data, i );
                i += 2;
                this.tileset = data[i++];
                this.dedicated = ( data[i++] == 1 );
        }
        LOG.info( "Done parsing." );
    }

    /**
     * This method is for parsing the SERVER_NEWGRFS-packet.
     *
     * @param data the data buffer received
     */
    void parseGRFNames( byte[] data )
    {
        LOG.debug( "Parsing GRF names." );
        int i = 0;
        int oldCount = grfRequests.length;
        int newCount = ParseUtil.parse8BitNumber( data, i++ );

        // Expand the GRFRequest array
        GRFRequest[] newArray = new GRFRequest[ oldCount + newCount ];
        System.arraycopy( grfRequests, 0, newArray, 0, oldCount );
        grfRequests = newArray;

        for ( int j = oldCount; j < ( oldCount + newCount ); j++ )
        {
            String id = Integer.toHexString( ParseUtil.parse8BitNumber( data, i++ ) ).toUpperCase();
            String md5 = "";
            for ( int k = 0; k < 16; k++ )
            {
                md5 += ParseUtil.parse32BitNumber( data, i );
                i += 4;
            }
            md5 = md5.toUpperCase();
            int length = ParseUtil.locateNextZero( data, i );
            String name = ParseUtil.parseString( data, i, length );
            i += length + 1;
            grfRequests[j] = new GRFRequest( id, md5, name );
        }
        LOG.debug( "Done parsing GRF names." );
    }

    /**
     * Getter for the server name.
     *
     * @return the server name
     */
    public String getServerName()
    {
        return serverName;
    }

    /**
     * Getter for the server version.
     *
     * @return the server version
     */
    public int[] getVersion()
    {
        return revision;
    }

    /**
     * Getter for string representation of version.
     *
     * @return the server version, e.g. 0.5.3
     */
    public String getVersionAsString()
    {
        String v = Integer.toString( revision[0] );
        for ( int i = 1; i < revision.length; i++ )
        {
            v += "." + revision[i];
        }
        return v;
    }

    /**
     * Getter for the server new graphics count.
     *
     * @return the new graphics count
     */
    public int getGraphicsCount()
    {
        return grfRequests.length;
    }

    /**
     * Method for getting the start date so that other formatting may be
     * applied.
     *
     * @return [0] = day(1-31), [1] = month(0-11), [2] = year
     */
    public int[] getStartDate()
    {
        return startDate;
    }

    /**
     * Convenience method for getting the start date in a formatted manner with
     * long month name.
     *
     * @return the formatted date
     */
    public String getLongStartDate()
    {
        return ParseUtil.getLongDate( startDate, Locale.UK );
    }

    /**
     * Convenience method for getting the start date in a formatted manner with
     * short month name.
     *
     * @return the formatted date
     */
    public String getShortStartDate()
    {
        return ParseUtil.getShortDate( startDate, Locale.UK );
    }

    /**
     * Method for getting the current game date so that other formatting may be
     * applied.
     *
     * @return [0] = day(1-31), [1] = month(0-11), [2] = year
     */
    public int[] getGameDate()
    {
        return gameDate;
    }

    /**
     * Convenience method for getting the game date in a formatted manner with
     * long month name.
     *
     * @return the formatted date
     */
    public String getLongGameDate()
    {
        return ParseUtil.getLongDate( gameDate, Locale.UK );
    }

    /**
     * Convenience method for getting the game date in a formatted manner with
     * short month name.
     *
     * @return the formatted date
     */
    public String getShortGameDate()
    {
        return ParseUtil.getShortDate( gameDate, Locale.UK );
    }

    /**
     * Getter for maximum number of companies
     *
     * @return the number
     */
    public int getMaxNumberOfCompanies()
    {
        return companies_max;
    }

    /**
     * Getter for the number of active companies.
     *
     * @return the number
     */
    public int getNumberOfActiveCompanies()
    {
        return companies_on;
    }

    /**
     * Getter for maximum number of specatators.
     *
     * @return the number
     */
    public int getMaxNumberOfSpectators()
    {
        return spectators_max;
    }

    /**
     * Getter for the number of active spectators.
     *
     * @return the number
     */
    public int getNumberOfActiveSpectators()
    {
        return spectators_on;
    }

    /**
     * Getter for maximum number of clients
     *
     * @return the number
     */
    public int getMaxNumberOfClients()
    {
        return clients_max;
    }

    /**
     * Getter for the number of active clients.
     *
     * @return the number
     */
    public int getNumberOfActiveClients()
    {
        return clients_on;
    }

    /**
     * Method for finding out whether or not the server is password protected.
     *
     * @return whether or not the server is password protected
     */
    public boolean isPasswordProtected()
    {
        return password_protected;
    }

    /**
     * Getter for the server's language index.
     *
     * @return the language index
     * @see #getServerLanguageAsString()
     */
    public int getServerLanguage()
    {
        return serverLang;
    }

    /**
     * Getter for the server's language.
     *
     * @return the language see #getServerLanguage()
     */
    public String getServerLanguageAsString()
    {
        return languages[serverLang];
    }

    /**
     * Method for finding out whether or not the server is running in dedicated
     * mode or if it is participating as a client.
     *
     * @return whether or not the serveris running in dedicated mode
     */
    public boolean isDedicated()
    {
        return dedicated;
    }

    /**
     * Getter for the meaning of the tileset integer.
     *
     * @return the string representation
     */
    public String getTilesetAsString()
    {
        return tilesets[tileset];
    }

    /**
     * Getter for the tileset of the map
     *
     * @return the integer representation
     */
    public int getTileset()
    {
        return tileset;
    }

    /**
     * Getter for the current map's width.
     *
     * @return the width
     */
    public int getMapWidth()
    {
        return map_width;
    }

    /**
     * Getter for the current map's height.
     *
     * @return the height
     */
    public int getMapHeight()
    {
        return map_height;
    }

    /**
     * Getter for the current map's name
     *
     * @return the name
     */
    public String getMapName()
    {
        return map_name;
    }

    /**
     * Method for getting a textual representation of this object. Very useful
     * for debugging.
     *
     * @return a {@link String} containing all data.
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder( "\tServerResponseInfo:\n" );
        sb.append( "\t\tServer name: " ).append( getServerName() ).append( "\n" );
        sb.append( "\t\tOpenTTD version: " ).append( getVersionAsString() ).append( "\n" );
        sb.append( "\t\tNew graphics count: " ).append( getGraphicsCount() ).append( "\n" );
        sb.append( "\t\tStart date: " ).append( getLongStartDate() ).append( "\n" );
        sb.append( "\t\tGame date: " ).append( getLongGameDate() ).append( "\n" );
        sb.append( "\t\tOpenTTD version: " ).append( getVersionAsString() ).append( "\n" );
        sb.append( "\t\tMax companies: " ).append( getMaxNumberOfCompanies() ).append( "\n" );
        sb.append( "\t\tActive companies: " ).append( getNumberOfActiveCompanies() ).append( "\n" );
        sb.append( "\t\tMax clients:: " ).append( getMaxNumberOfClients() ).append( "\n" );
        sb.append( "\t\tActive clients: " ).append( getNumberOfActiveClients() ).append( "\n" );
        sb.append( "\t\tMax spectators: " ).append( getMaxNumberOfSpectators() ).append( "\n" );
        sb.append( "\t\tActive spectators: " ).append( getNumberOfActiveSpectators() ).append( "\n" );
        sb.append( "\t\tServer language: " ).append( getServerLanguageAsString() ).append( "\n" );
        sb.append( "\t\tPassword protection: " ).append( isPasswordProtected() ? "on" : "off" ).append( "\n" );
        sb.append( "\t\tDedicated: " ).append( isDedicated() ? "on" : "off" ).append( "\n" );
        sb.append( "\t\tMap width: " ).append( getMapWidth() ).append( "\n" );
        sb.append( "\t\tMap height: " ).append( getMapHeight() ).append( "\n" );
        sb.append( "\t\tTileset: " ).append( getTilesetAsString() ).append( "\n" );
        sb.append( "\t\tMap name: " ).append( getMapName() ).append( "\n" );
        return sb.toString();
    }
    /**
     * The different tilesets of OpenTTD
     */
    private static String[] tilesets = new String[]
    {
        "Temperate",
        "Arctic",
        "Desert",
        "Toyland"
    };
    /**
     * The different server languages of OpenTTD
     */
    private static String[] languages = new String[]
    {
        "Any",
        "English",
        "German",
        "French",
        "Brazilian",
        "Bulgarian",
        "Chinese",
        "Czech",
        "Danish",
        "Dutch",
        "Esperanto",
        "Finnish",
        "Hungarian",
        "Icelandic",
        "Italian",
        "Japanese",
        "Korean",
        "Lithuanian",
        "Norwegian",
        "Polish",
        "Portugese",
        "Romanian",
        "Russian",
        "Slovak",
        "Slovenian",
        "Spanish",
        "Swedish",
        "Turkish",
        "Ukranian"
    };
}
