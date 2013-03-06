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
package com.camelspotting.jotl.domain;

import com.camelspotting.jotl.NewGRF;
import java.util.List;
import org.joda.time.LocalDate;
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
public final class ServerDetails
{

    /**
     * The logger object for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger( ServerDetails.class );
    /**
     * The NewGRFs in use
     */
    private List<NewGRF> newGRFs;
    /**
     * The server's name
     */
    private String serverName;
    /**
     * Holds the current game date
     */
    private LocalDate gameDate;
    /**
     * Holds the game's start date
     */
    private LocalDate startDate;
    /**
     * The maximum allowed companies
     */
    private int maxCompanies;
    /**
     * The number of companies on at the moment
     */
    private int onCompanies;
    /**
     * The maximum allowed spectators
     */
    private int maxSpectators;
    /**
     * The number of spectators on at the moment
     */
    private int onSpectators;
    /**
     * The maximum allowed clients
     */
    private int maxClients;
    /**
     * The number of clients on at the moment
     */
    private int onClients;
    /**
     * The game version
     */
    private String gameVersion;
    /**
     * The language the server is running
     */
    private int serverLang;
    /**
     * Whether the server is password protected
     */
    private boolean passwordProtected;
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
    private int mapHeight;
    /**
     * The current map's width
     */
    private int mapWidth;
    /**
     * The current map's name
     */
    private String mapName;

    public ServerDetails( List<NewGRF> newGRFs, String serverName, LocalDate gameDate, LocalDate startDate, int maxCompanies, int onCompanies, int maxSpectators, int onSpectators, int maxClients, int onClients, String gameVersion, int serverLang, boolean passwordProtected, boolean dedicated, int tileset, int mapHeight, int mapWidth, String mapName )
    {
        this.newGRFs = newGRFs;
        this.serverName = serverName;
        this.gameDate = gameDate;
        this.startDate = startDate;
        this.maxCompanies = maxCompanies;
        this.onCompanies = onCompanies;
        this.maxSpectators = maxSpectators;
        this.onSpectators = onSpectators;
        this.maxClients = maxClients;
        this.onClients = onClients;
        this.gameVersion = gameVersion;
        this.serverLang = serverLang;
        this.passwordProtected = passwordProtected;
        this.dedicated = dedicated;
        this.tileset = tileset;
        this.mapHeight = mapHeight;
        this.mapWidth = mapWidth;
        this.mapName = mapName;
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
     * Getter for the game version.
     *
     * @return the game version
     */
    public String getVersion()
    {
        return gameVersion;
    }

    /**
     * Getter for game version as components: major.minor.revision
     *
     * @return the server version, e.g. 0.5.3
     */
    public int[] getVersionComponents()
    {
        String[] A = gameVersion.split( "\\." );
        int[] B = new int[ A.length ];
        for ( int i = 0; i < A.length; i++ )
        {
            B[i] = Integer.valueOf( A[i] );
        }
        return B;
    }

    /**
     * Getter for the server new graphics count.
     *
     * @return the new graphics count
     */
    public int getGraphicsCount()
    {
        return newGRFs != null ? newGRFs.size() : 0;
    }

    public List<NewGRF> getNewGRFs()
    {
        return newGRFs;
    }

    /**
     * Method for getting the start date so that other formatting may be
     * applied.
     *
     * @return date
     */
    public LocalDate getStartDate()
    {
        return startDate;
    }

    /**
     * Method for getting the current game date so that other formatting may be
     * applied.
     *
     * @return date
     */
    public LocalDate getGameDate()
    {
        return gameDate;
    }

    /**
     * Getter for maximum number of companies
     *
     * @return the number
     */
    public int getMaxNumberOfCompanies()
    {
        return maxCompanies;
    }

    /**
     * Getter for the number of active companies.
     *
     * @return the number
     */
    public int getNumberOfActiveCompanies()
    {
        return onCompanies;
    }

    /**
     * Getter for maximum number of specatators.
     *
     * @return the number
     */
    public int getMaxNumberOfSpectators()
    {
        return maxSpectators;
    }

    /**
     * Getter for the number of active spectators.
     *
     * @return the number
     */
    public int getNumberOfActiveSpectators()
    {
        return onSpectators;
    }

    /**
     * Getter for maximum number of clients
     *
     * @return the number
     */
    public int getMaxNumberOfClients()
    {
        return maxClients;
    }

    /**
     * Getter for the number of active clients.
     *
     * @return the number
     */
    public int getNumberOfActiveClients()
    {
        return onClients;
    }

    /**
     * Method for finding out whether or not the server is password protected.
     *
     * @return whether or not the server is password protected
     */
    public boolean isPasswordProtected()
    {
        return passwordProtected;
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
        return mapWidth;
    }

    /**
     * Getter for the current map's height.
     *
     * @return the height
     */
    public int getMapHeight()
    {
        return mapHeight;
    }

    /**
     * Getter for the current map's name
     *
     * @return the name
     */
    public String getMapName()
    {
        return mapName;
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
        sb.append( "\t\tOpenTTD version: " ).append( getVersion() ).append( "\n" );
        sb.append( "\t\tNew graphics count: " ).append( getGraphicsCount() ).append( "\n" );
        sb.append( "\t\tStart date: " ).append( getStartDate().toString() ).append( "\n" );
        sb.append( "\t\tGame date: " ).append( getGameDate().toString() ).append( "\n" );
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
