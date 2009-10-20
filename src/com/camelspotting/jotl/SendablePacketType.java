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

import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * This enum is for enumerating the different types of UDP packets
 * that the OpenTTD server will understand and aknowledge.
 * @author Eivind Brandth Smedseng
 * @author Mats Andreassen
 * @version 1.0
 * @see RecievablePacketType
 */
public enum SendablePacketType {

    /** Queries a game server for game information */
    CLIENT_FIND_SERVER(0),
    /** Queries a game server about details of the game, such as companies */
    CLIENT_DETAIL_INFO(2);
    //SERVER_REGISTER(4),       // Packet to register itself to the master server
    //CLIENT_GET_LIST(6),       // Request for serverlist from master server
    //SERVER_UNREGISTER(8),     // Request to be removed from the server-list
    //CLIENT_GET_NEWGRFS(9);      // Requests the names for a list of GRFs (GRF_ID and MD5)
    /** Value sent to the OpenTTD-server to indicate type of package */
    private int value;

    /**
     * Smple constructor.
     * @param value     the final byte for the packet
     */
    private SendablePacketType(int value) {
        this.value = value;
    }

    /**
     * This method constructs a {@link DatagramPacket} ready for send-off
     * to the OpenTTD-server.
     * NOTE: This method is not utilized in the case of the GET_NEWGRFS-packet.
     * @param address       where to send it
     * @param destPort      which port to contact
     * @return              the ready {@link DatagramPacket}
     */
    public DatagramPacket createPacket(InetAddress address,int destPort) {
        byte[] q = new byte[]{(byte) 3, (byte) 0, (byte) value};
        return new DatagramPacket(q, q.length, address, destPort);
    }
}
