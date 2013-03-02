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

import com.camelspotting.jotl.domain.Company;
import com.camelspotting.jotl.exceptions.JOTLException;
import com.camelspotting.jotl.domain.Client;
import com.camelspotting.jotl.parsing.ParseUtil;
import com.camelspotting.jotl.parsing.Station;
import com.camelspotting.jotl.parsing.Vehicle;
import com.camelspotting.jotl.udp.BitUtil;
import com.camelspotting.jotl.udp.DateUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is for holding the servers detailed info.
 *
 * @author Mats Andreassen
 * @version 1.0
 * @see SendablePacketType
 */
public final class ServerInfo
{

    /**
     * The logger object for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger( ClientsInfo.class );
    /**
     * All companies
     */
    private List<Company> companies;
    /**
     * All clients, both spectators and players
     */
    private List<Client> allClients;
    /**
     * This holds the UDP packet version
     */
    private int version;

    /**
     * The constructor parses the buffer for all interesting data that is
     * contained within.
     *
     * @param data the buffer
     */
    public ServerInfo( byte[] data )
    {
        this.companies = new ArrayList<Company>();
        this.allClients = new ArrayList<Client>();
        int i = 3;
        this.version = data[i++];
        int activePlayers = data[i++];

        switch ( this.version )
        {
            case 6:
            case 5:
                parseVersion5( data, activePlayers, i );
                break;
            case 4:
            case 3:
            case 2:
            case 1:
                parseVersion4( data, activePlayers, i );
                break;
            default:
                LOG.info( "Unknown version detected." );
                break;
        }
        LOG.info( "Done parsing." );
    }

    private void parseVersion5( byte[] data, int activePlayers, int i )
    {
        LOG.info( "Parsing version 5 info." );
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

            Company com = new Company( current, compName, inaugurated, companyValue, money, income, performance, passwordProtected );
            LOG.debug( "Created {}.", com );
            companies.add( com );

            /* vehicle info */
            for ( Vehicle v : Vehicle.values() )
            {
                com.setNumberOfVehicles( v, BitUtil.parse16BitNumber( data, i ) );
                i += 2;
            }
            LOG.debug( "{} has {} vehicles.", com.getCurrentId(), com.getNumberOfVehicles() );

            /* station info */
            for ( Station s : Station.values() )
            {
                com.setNumberOfStations( s, BitUtil.parse16BitNumber( data, i ) );
                i += 2;
            }
            LOG.debug( "{} has {} stations.", com.getCurrentId(), com.getNumberOfStations() );
        }
    }

    private void parseVersion4( byte[] data, int activePlayers, int i )
    {
        LOG.info( "Parsing version 4 info." );
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

            Company com = new Company( current, compName, inaugurated, companyValue, money, income, performance, passwordProtected );
            LOG.debug( "Created company: {}", com );
            companies.add( com );

            /* vehicle info */
            for ( Vehicle v : Vehicle.values() )
            {
                com.setNumberOfVehicles( v, BitUtil.parse16BitNumber( data, i ) );
                i += 2;
            }
            LOG.debug( "{} has {} vehicles.", com.getCurrentId(), com.getNumberOfVehicles() );

            /* station info */
            for ( Station s : Station.values() )
            {
                com.setNumberOfStations( s, BitUtil.parse16BitNumber( data, i ) );
                i += 2;
            }
            LOG.debug( "{} has {} stations.", com.getCurrentId(), com.getNumberOfStations() );

            /* Get a list of clients connected to this company.
             * At this point we read a boolean value from the buffer, if > 0 there is another client
             */
            while ( data[i++] > 0 )
            {
                length = ParseUtil.locateNextZero( data, i );
                String cName = ParseUtil.parseString( data, i, length );
                i += length + 1;
                length = ParseUtil.locateNextZero( data, i );
                String uniqueId = ParseUtil.parseString( data, i, length );
                i += length + 1;
                // join_date is transmitted in 
                LocalDate joinDate = DateUtil.convertDateToYMD(BitUtil.parse32BitNumber( data, i ) );
                i += 4;

                Client client = new Client( cName, uniqueId, joinDate, false, com );
                LOG.debug( "Found '{}' connected to company {}.", client, com.getCurrentId() );
                com.addClient( client );
                allClients.add( client );
            }
        }

        // Now let's parse any spectators
        while ( data[i++] > 0 )
        {
            int length = ParseUtil.locateNextZero( data, i );
            String cName = ParseUtil.parseString( data, i, length );
            // If this is the unreal spectator that is always present on the server.
            if ( cName.equals( "" ) )
            {
                LOG.debug( "Ignoring unreal spectator." );
                continue;
            }
            i += length + 1;
            length = ParseUtil.locateNextZero( data, i );
            String uniqueId = ParseUtil.parseString( data, i, length );
            i += length + 1;
            LocalDate joinDate = DateUtil.convertDateToYMD( BitUtil.parse32BitNumber( data, i ) );
            i += 4;

            Client client = new Client( cName, uniqueId, joinDate, true, null );
            LOG.debug( "Found spectator '" + client + "'." );
            allClients.add( client );
        }
    }

    /**
     * Method for getting all {@link Client}s.
     *
     * @return an {@link ArrayList} of {@link Client}
     */
    public List<Client> getAllClients() throws JOTLException
    {
        if ( getVersion() == 5 )
        {
            throw new JOTLException( "This data is not available in version 5(OpenTTD version 0.6.2)." );
        }
        return Collections.unmodifiableList( allClients );
    }

    /**
     * This method is for getting a {@link ArrayList} of all connected players.
     *
     * @return a copy of the list
     */
    public List<Client> getPlayers() throws JOTLException
    {
        return getClients( true );
    }

    /**
     * This method is for getting a {@link ArrayList} of all connected
     * spectators.
     *
     * @return a copy of the list
     */
    public List<Client> getSpectators() throws JOTLException
    {
        return getClients( false );
    }

    /**
     * This is a support method for getSpectators() and getPlayers().
     *
     * @param player true to get players, false to get spectators
     * @return a copy of the list
     * @see #getPlayers()
     * @see #getSpectators()
     */
    private List<Client> getClients( boolean player ) throws JOTLException
    {
        if ( getVersion() == 5 )
        {
            throw new JOTLException( "This data is not available in version 5(OpenTTD version 0.6.2)." );
        }
        Collections.sort( allClients );
        List<Client> list = new ArrayList<Client>();
        for ( Client c : allClients )
        {
            if ( c.isSpectator() != player )
            {
                list.add( c );
            }
        }
        return list;
    }

    /**
     * This method is for getting a {@link ArrayList} of all companies currently
     * in play.
     *
     * @return a copy of the list
     */
    public List<Company> getCompanies()
    {
        Collections.sort( companies );
        return Collections.unmodifiableList( companies );
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
        StringBuilder sb = new StringBuilder( "\tServerDetailedInfo:\n" );
        sb.append( "\t\tCompanies: \n" );
        for ( Company com : getCompanies() )
        {
            sb.append( "\t\t\t" ).append( com ).append( "\n" );
            List<Client> clients = com.getClients();
            if ( getVersion() >= 5 && clients.size() > 0 )
            {
                sb.append( "\t\t\t\tClients:\n" );
                for ( Client c : clients )
                {
                    sb.append( "\t\t\t\t\t" ).append( c ).append( "\n" );
                }
            }
        }

        try
        {
            List<Client> clients = getSpectators();
            if ( getVersion() >= 5 && clients.size() > 0 )
            {
                sb.append( "\t\tSpectators:\n" );
                for ( Client c : clients )
                {
                    sb.append( "\t\t\t" ).append( c ).append( "\n" );
                }
            }
        }
        catch ( JOTLException ex )
        {
            LOG.error( ex.getMessage(), ex );
        }
        return sb.toString();
    }

    /**
     * Getter for the UDP-packet version.
     *
     * @return the version
     */
    public int getVersion()
    {
        return version;
    }
}
