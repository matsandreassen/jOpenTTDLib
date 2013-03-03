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

import com.camelspotting.jotl.ServerHandler;

/**
 * This method is is just a wrapper for response and detail information, and is
 * primarily used for archiving the final query result for games in
 * {@link ServerHandler}.
 *
 * @author Mats Andreassen
 * @version 1.0
 * @see ServerHandler
 */
public class Game
{

    /**
     * Incrementable ID to label games
     */
    private static int id_inc = 0;
    /**
     * One of the objects that will contain gathered information
     */
    private final ServerDetails clientsInfo;
    /**
     * One of the objects that will contain gathered information
     */
    private final ClientsDetails serverInfo;
    /**
     * Game ID
     */
    private final int id;

    /**
     * This internal constructor is for creating these archive objects.
     *
     * @param clientsInfo the information to contain
     * @param serverInfo more information to contain
     */
    public Game( ServerDetails sri, ClientsDetails sdi )
    {
        this.id = ++Game.id_inc;
        this.clientsInfo = sri;
        this.serverInfo = sdi;
    }

    /**
     * Getter for the {@link ClientsInfo} object.
     *
     * @return the {@link ClientsInfo} object
     */
    public ClientsDetails getServerInfo()
    {
        return serverInfo;
    }

    /**
     * Getter for the {@link ServerInfo} object.
     *
     * @return the {@link ServerInfo} object
     */
    public ServerDetails getClientsInfo()
    {
        return clientsInfo;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder( "Game: \n" );
        sb.append( serverInfo.toString() );
        sb.append( clientsInfo.toString() );
        return sb.toString();
    }
}
