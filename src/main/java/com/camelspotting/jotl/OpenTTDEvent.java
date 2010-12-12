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

import java.util.Arrays;

/**
 * This class represents the events that may occur during
 * an OpenTTD-game.
 * NOTE: The electric rail availability may
 * be falsely reported since it can be deactivated
 * with a patch and such a deactivation is undetectable through
 * communication with the server.
 * @author Mats Andreassen
 * @version 1.0
 */
public class OpenTTDEvent {

    /** The type of event*/
    private Type type;
    /** Objects of interest */
    private Object[] objects;

    /**
     * Constructor for making events.
     * @param t     the type of event
     */
    OpenTTDEvent(Type t,Object... objects) {
        this.type = t;
        this.objects = objects;
    }
    
    /**
     * Depending on what type of event this is this array contains different
     * objects.
     * GAME_END           -> [0] is an {@link Integer} containing the year of this event
     * GAME_START         -> [0] is an {@link Integer} containing the year of this event
     * GAME_IN_PROGRESS   -> [0] is an {@link Integer} containing the year of this event
     * NEW_LEADER         -> [0] is the {@link Company} now in the lead
     * ELECTRIC_AVAILABLE -> nothing
     * MONORAIL_AVAILABLE -> nothing
     * MAGLEV_AVAILABLE   -> nothing
     * COMPANY_NEW        -> [0] is the {@link Company}
     * COMPANY_REMOVED    -> [0] is the removed {@link Company}
     * PAUSED             -> [0] is an {@link Integer} containing the year of this event
     * UNPAUSED           -> [0] is an {@link Integer} containing the year of this event
     * CLIENT_JOIN        -> [0] is the {@link Client} that just joined
     * CLIENT_LEFT        -> [0] is the {@link Client} that just left
     * LOST_CONNECTION    -> nothing
     * COMPANY_REMOVED    -> [0] is the {@link Company}
     * @return      an Object array of length at least 0.
     */
    public Object[] getObjects() {
        return (objects != null) ? objects : new Object[0];
    }

    /**
     * This method returns the type of event.
     * @return      the type
     */
    public Type getType() {
        return type;
    }
    
    /**
     * This gives a description of this event.
     * @return      the description of the type and the list of appended objects
     */
    @Override
    public String toString() {
        return new StringBuilder("OpenTTDEvent(").append(type.desc).append(") Objects: ").append(Arrays.toString(objects)).toString();
    }

    /**
     * This enum represents the type of OpenTTD events.
     * @author Mats Andreassen
     * @version 1.0
     */
    public enum Type {
        /** A game has ended */
        GAME_END("Game end"),
        /** A game has started */
        GAME_START("Game start"),
        /** A game is in progress*/
        GAME_IN_PROGRESS("Game in progress"),
        /** A new company now has the best rating */
        NEW_LEADER("New leader"),
        /** Electric rail has now become available */
        ELECTRIC_AVAILABLE("Electric rail"),
        /** Monorail has now become available */
        MONORAIL_AVAILABLE("Monorail"),
        /** Maglev has now become available */
        MAGLEV_AVAILABLE("Maglev"),
        /** A new company has been created */
        COMPANY_NEW("New company"),
        /** A company has been removed */
        COMPANY_REMOVED("Company removed"),
        /** The game has been paused */
        PAUSED("Paused"),
        /** The game has been unpaused */
        UNPAUSED("Unpaused"),
        /** A client has joined */
        CLIENT_JOIN("Client joined"),
        /** A client has left */
        CLIENT_LEFT("Client left"),
        /** Software has lost connection to server */
        LOST_CONNECTION("Lost connection");

        /** This is a description of the type */
        private String desc;
        
        /**
         * Constructor for enum.
         * @param desc      a description for types
         */
        private Type(String desc) {
            this.desc = desc;
        }
        
        /**
         * Returns a textual description of the enum.
         * @return      the description
         */
        @Override
        public String toString() {
            return desc;
        }
    }
}
