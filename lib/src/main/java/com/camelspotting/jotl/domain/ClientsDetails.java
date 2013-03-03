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

import com.camelspotting.jotl.domain.Company;
import com.camelspotting.jotl.domain.Client;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is for holding the servers detailed info.
 *
 * @author Mats Andreassen
 * @version 1.0
 * @see SendablePacketType
 */
public class ClientsDetails
{

    /**
     * The logger object for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger( ServerDetails.class );
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

    public ClientsDetails( List<Company> companies, List<Client> allClients, int version )
    {
        this.companies = companies;
        this.allClients = allClients;
        this.version = version;
    }

    /**
     * Method for getting all {@link Client}s.
     *
     * @return an {@link ArrayList} of {@link Client}
     */
    public List<Client> getClients()
    {
        if ( getVersion() == 5 )
        {
            throw new UnsupportedOperationException( "This data is not available in UDP version 5." );
        }
        return Collections.unmodifiableList( allClients );
    }

    /**
     * This method is for getting a {@link ArrayList} of all connected players.
     *
     * @return a copy of the list
     */
    public List<Client> getPlayers()
    {
        return getClients( true );
    }

    /**
     * This method is for getting a {@link ArrayList} of all connected
     * spectators.
     *
     * @return a copy of the list
     */
    public List<Client> getSpectators()
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
    private List<Client> getClients( boolean player )
    {
        if ( getVersion() == 5 )
        {
            throw new UnsupportedOperationException( "This data is not available in UDP version 5." );
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
        StringBuilder sb = new StringBuilder( "\tClientsDetails:\n" );
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

        List<Client> clients = getSpectators();
        if ( getVersion() >= 5 && clients.size() > 0 )
        {
            sb.append( "\t\tSpectators:\n" );
            for ( Client c : clients )
            {
                sb.append( "\t\t\t" ).append( c ).append( "\n" );
            }
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
