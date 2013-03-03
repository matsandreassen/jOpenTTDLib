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
public class ClientsDetailsV4 extends AbstractClientDetails
{

    /**
     * The logger object for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger( ServerDetails.class );
    /**
     * All clients, both spectators and players
     */
    private List<Client> allClients;

    public ClientsDetailsV4( List<Company> companies, List<Client> allClients )
    {
        super( companies );
        this.allClients = allClients;
    }

    /**
     * Method for getting all {@link Client}s.
     *
     * @return an {@link ArrayList} of {@link Client}
     */
    @Override
    public List<Client> getClients()
    {
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
}
