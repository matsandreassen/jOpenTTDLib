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
 * Any classes that is to monitor an OpenTTD-server and it's games will
 * have to implement this interface and register with the {@link ServerHandler}
 * to recieve events.
 * @author Mats Andreassen
 * @version 1.0
 */
public interface OpenTTDListener {
    
    /**
     * Method is invoked every time an event occurs. 
     * Check {@link OpenTTDEvent} to see
     * what events are triggered.
     * @param evt       the event triggered
     * @see OpenTTDEvent
     */
    public void eventOccured(OpenTTDEvent evt);
}
