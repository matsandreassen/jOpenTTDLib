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
 * This enum is for enumerating the different types of UDP packets that the
 * OpenTTD server will understand and aknowledge.
 *
 * @author Eivind Brandth Smedseng
 * @author Mats Andreassen
 * @version 1.0
 * @see RecievablePacketType
 */
public enum PacketType
{

    /**
     * Queries a game server for game information
     */
    CLIENT_FIND_SERVER( 0, PacketOrigin.CLIENT ),
    /**
     * Reply of the game server with game information
     */
    SERVER_RESPONSE( 1, PacketOrigin.SERVER ),
    /**
     * Queries a game server about details of the game, such as companies
     */
    CLIENT_DETAIL_INFO( 2, PacketOrigin.CLIENT ),
    /**
     * Reply from the game server about details of the game, such as companies
     */
    SERVER_DETAIL_INFO( 3, PacketOrigin.SERVER ),
    /**
     * Packet to register itself to the master server
     */
    SERVER_REGISTER( 4, PacketOrigin.SERVER ),
    /**
     * Packet indicating registration has succedeed
     */
    MASTER_ACK_REGISTER( 5, PacketOrigin.MASTER_SERVER ),
    /**
     * Request for serverlist from master server
     */
    CLIENT_GET_LIST( 6, PacketOrigin.CLIENT ),
    /**
     * Response from master server with server ip's + port's
     */
    MASTER_RESPONSE_LIST( 7, PacketOrigin.MASTER_SERVER ),
    /**
     * Request to be removed from the server-list
     */
    SERVER_UNREGISTER( 8, PacketOrigin.SERVER ),
    /**
     * Requests the names for a list of GRFs (GRF_ID and MD5)
     */
    CLIENT_GET_NEWGRFS( 9, PacketOrigin.CLIENT ),
    /**
     * Sends the list of NewGRF's requested.
     */
    SERVER_NEWGRFS( 10, PacketOrigin.SERVER );
    /**
     * Value sent to the OpenTTD-server to indicate type of package
     */
    private final int value;
    /**
     * Where does this packet type come from?
     */
    private final PacketOrigin packetOrigin;

    /**
     * Smple constructor.
     *
     * @param value the final byte for the packet
     */
    private PacketType( int value, PacketOrigin packetOrigin )
    {
        this.value = value;
        this.packetOrigin = packetOrigin;
    }

    /**
     * Where does this packet type come from?
     *
     * @return
     */
    public PacketOrigin getPacketOrigin()
    {
        return packetOrigin;
    }

    /**
     * This method constructs a {@link DatagramPacket} ready for send-off to the
     * OpenTTD-server. NOTE: This method is not utilized in the case of the
     * GET_NEWGRFS-packet.
     *
     * @param address where to send it
     * @param destPort which port to contact
     * @return the ready {@link DatagramPacket}
     */
    public DatagramPacket createPacket( InetAddress address, int destPort )
    {
        byte[] q = new byte[]
        {
            (byte) 3, (byte) 0, (byte) value
        };
        return new DatagramPacket( q, q.length, address, destPort );
    }

    public static PacketType fromInt( int value )
    {
        for ( PacketType pt : values() )
        {
            if ( pt.value == value )
            {
                return pt;
            }
        }
        throw new IllegalArgumentException( String.format( "Unknown packet type: %d", value ) );
    }

    public static enum PacketOrigin
    {

        MASTER_SERVER, SERVER, CLIENT;
    }
}
