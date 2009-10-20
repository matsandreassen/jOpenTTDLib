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
 * This enum is for enumerating the different types of UDP packets
 * that the OpenTTD server will be sending in reply any packet we might send.
 * @author Eivind Brandth Smedseng
 * @author Mats Andreassen
 * @version 1.0
 * @see SendablePacketType
 */
public enum RecievablePacketType {

	/** Reply from the game server with game information */
	SERVER_RESPONSE(1),
	/** Reply from the game server about details of the game, such as companies */
	SERVER_DETAIL_INFO(3),
	//    MASTER_ACK_REGISTER(5),   // Packet indicating registration has succedeed
	//    MASTER_RESPONSE_LIST(7),  // Response from master server with server ip's + port's
	/** Reply from server with list of NewGRF's requested */
	SERVER_NEWGRFS(10);
//    END(11);                  // ?
	/** Value received from the OpenTTD-server to indicate type of package */
	private int value;

	/**
	 * Smple constructor.
	 * @param value     the final byte for the packet
	 */
	private RecievablePacketType(int value) {
		this.value = value;
	}

	/**
	 * Method for getting the enum corresponding
	 * to the value of a packet that is received.
	 * @param value     the packet type to look for
	 * @return          the enum in question or null if we don't "understand" this packet
	 */
	static public RecievablePacketType getEnum(int value) {
		for (RecievablePacketType rpt : values()) {
			if (rpt.value == value) {
				return rpt;
			}
		}
		return null;
	}
}
