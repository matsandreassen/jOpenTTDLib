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
import java.util.Locale;

/**
 * This class represents a connected client,
 * both player and spectator.
 * @author Mats Andreassen
 * @version 1.0
 */
public final class Client implements Comparable<Client> {

    /** The name of the client */
    private String name;
    /** A non-spectating client must be connected to a company */
    private Company comp;
    /** A unique ID identifying the client */
    private String uniqueId;
    /** What date this client joined the game */
    private int[] joinDate;
    /** Whether or not the client is a spectator */
    private boolean spectator;

    /**
     * Constructor for clients.
     * @param name          the name of the client
     * @param uniqueId      a unique ID identifying the client
     * @param joinDate      what date this client joined the game
     * @param spectator     whether or not the client is a spectator
     * @param com           the company the client is connected to
     */
    Client(String name, String uniqueId, int[] joinDate, boolean spectator, Company com) {
        this.name = name;
        this.uniqueId = uniqueId;
        this.joinDate = joinDate;
        this.spectator = spectator;
        this.comp = com;
    }

    /**
     * This method is for getting the {@link Company} that the client is
     * connected to.
     * @return      the company or null if the client is spectating
     */
    public Company getCompany() {
        if (!spectator) {
            return comp;
        } else {
            return null;
        }
    }

    /**
     * Getter for a clients name.
     * @return  the name
     */
    public String getName() {
        return name;
    }

    /**
     * Getter for a clients unique id.
     * @return  the id
     */
    public String getUniqueId() {
        return uniqueId;
    }

    /**
     * Getter for a clients join date in long format.
     * @return  the join date
     */
    public String getLongJoinDate() {
        return Parser.getLongDate(joinDate, Locale.UK);
    }

    /**
     * Getter for a clients join date in short format.
     * @return  the join date
     */
    public String getShortJoinDate() {
        return Parser.getShortDate(joinDate, Locale.UK);
    }

    /**
     * Getter for a clients spectator status.
     * @return  false if the client is playing and true otherwise
     */
    public boolean isSpectator() {
        return spectator;
    }

    /**
     * Convenience method for printing out the information of this client.
     * @return      the information
     */
    @Override
    public String toString() {
        return new StringBuilder(isSpectator() ? "Spectator" : "Client").append(": name, ").append(getName()).append(", join date: ").append(getLongJoinDate()).toString();
    }

    /**
     * This method makes the clients comparable based on their names.
     * @param c     the client to compare to
     * @return      Less than 0 if this client is 'less' than the other, 0 if they're equal, more than 1 if is 'more' than the other
     */
    @Override
    public int compareTo(Client c) {
        return name.compareTo(c.getName());
    }

    /**
     * This method will return whether or not this {@link Client} is equal to another
     * object by comparing their unique id's if the other object is also a Client.
     * If it is not a client, it reverts to default behaviour.
     * @param o     the object to compare this with
     * @return      whether or not this {@link Client} is equal to another object
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof Client) {
            Client c = (Client) o;
            return uniqueId.equals(c.getUniqueId());
        } else {
            return super.equals(o);
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.uniqueId != null ? this.uniqueId.hashCode() : 0);
        hash = 37 * hash + Arrays.hashCode(this.joinDate);
        return hash;
    }
}
