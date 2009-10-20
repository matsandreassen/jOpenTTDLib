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

/**
 * This method is is just a wrapper for response and detail information, and
 * is primarily used for archiving the final query result for games in {@link ServerHandler}.
 * @author Mats Andreassen
 * @version 1.0
 * @see ServerHandler
 */
public class Game {

    /** Incrementable ID to label games*/
    private static int id_inc = 0;
    /** One of the objects that will contain gathered information */
    private ServerResponseInfo sri;
    /** One of the objects that will contain gathered information */
    private ServerDetailedInfo sdi;
    /** Game ID */
    private int id;

    /**
     * This internal constructor is for creating these
     * archive objects.
     * @param sri       the information to contain
     * @param sdi       more information to contain
     */
    Game(ServerResponseInfo sri, ServerDetailedInfo sdi) {
        this.id = ++Game.id_inc;
        this.sri = sri;
        this.sdi = sdi;
    }
    
    /**
     * Getter for the {@link ServerResponseInfo} object.
     * @return      the {@link ServerResponseInfo} object
     */
    public ServerResponseInfo getServerResponseInfo() {
        return sri;
    }
    
    /**
     * Getter for the {@link ServerDetailedInfo} object.
     * @return      the {@link ServerDetailedInfo} object
     */
    public ServerDetailedInfo getServerDetailedInfo() {
        return sdi;
    }
}
