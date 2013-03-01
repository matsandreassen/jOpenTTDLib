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
package com.camelspotting.jotl.event;

/**
 * This enum represents the type of OpenTTD events.
 *
 * @author Mats Andreassen
 * @version 1.0
 */
public enum OpenTTDEventType
{

    /**
     * A game has ended
     */
    GAME_END( "Game end" ),
    /**
     * A game has started
     */
    GAME_START( "Game start" ),
    /**
     * A game is in progress
     */
    GAME_IN_PROGRESS( "Game in progress" ),
    /**
     * A new company now has the best rating
     */
    NEW_LEADER( "New leader" ),
    /**
     * Electric rail has now become available
     */
    ELECTRIC_AVAILABLE( "Electric rail" ),
    /**
     * Monorail has now become available
     */
    MONORAIL_AVAILABLE( "Monorail" ),
    /**
     * Maglev has now become available
     */
    MAGLEV_AVAILABLE( "Maglev" ),
    /**
     * A new company has been created
     */
    COMPANY_NEW( "New company" ),
    /**
     * A company has been removed
     */
    COMPANY_REMOVED( "Company removed" ),
    /**
     * The game has been paused
     */
    PAUSED( "Paused" ),
    /**
     * The game has been unpaused
     */
    UNPAUSED( "Unpaused" ),
    /**
     * A client has joined
     */
    CLIENT_JOIN( "Client joined" ),
    /**
     * A client has left
     */
    CLIENT_LEFT( "Client left" ),
    /**
     * Software has lost connection to server
     */
    LOST_CONNECTION( "Lost connection" );
    /**
     * This is a description of the type
     */
    private String description;

    /**
     * Constructor for enum.
     *
     * @param description a description for types
     */
    private OpenTTDEventType( String desc )
    {
        this.description = desc;
    }

    /**
     * Returns a textual description of the enum.
     *
     * @return the description
     */
    @Override
    public String toString()
    {
        return getDescription();
    }

    /**
     * @return the description
     */
    public String getDescription()
    {
        return description;
    }
}
