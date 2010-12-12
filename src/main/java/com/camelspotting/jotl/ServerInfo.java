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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is for holding the servers detailed info.
 * @author Mats Andreassen
 * @version 1.0
 * @see SendablePacketType
 */
public final class ServerInfo {

    /** The logger object for this class */
    private static final Logger LOG = LoggerFactory.getLogger(ClientsInfo.class);
    /** All companies */
    private List<Company> companies;
    /** All clients, both spectators and players */
    private List<Client> allClients;
    /** This holds the UDP packet version */
    private int version;

    /**
     * The constructor parses the buffer for all interesting data that
     * is contained within.
     * @param data      the buffer
     */
    public ServerInfo(byte[] data) {
        this.companies = new ArrayList<Company>();
        this.allClients = new ArrayList<Client>();
        int i = 3;
        this.version = data[i++];
        int activePlayers = data[i++];

        switch (this.version) {
            case 6:
            case 5:
                parseVersion5(data, activePlayers, i);
                break;
            case 4:
            case 3:
            case 2:
            case 1:
                parseVersion4(data, activePlayers, i);
                break;
            default:
                LOG.info("Unknown version detected.");
                break;
        }
        LOG.info("Done parsing.");
    }

    private void parseVersion5(byte[] data, int activePlayers, int i) {
        LOG.info("Parsing version 5 info.");
        for (int j = 0; j < activePlayers; j++) {
            int current = data[i++];
            int length = Parser.locateNextZero(data, i);
            LOG.debug("New company name seems to be " + length + " characters long.");
            String compName = Parser.parseString(data, i, length);
            i += length + 1;
            int inaugurated = Parser.parse32BitNumber(data, i);
            i += 4;
            long companyValue = Parser.parse64BitNumber(data, i);
            i += 8;
            long money = Parser.parse64BitNumber(data, i);
            i += 8;
            long income = Parser.parse64BitNumber(data, i);
            i += 8;
            int performance = Parser.parse16BitNumber(data, i);
            i += 2;
            boolean passwordProtected = (data[i++] == 1);

            LOG.debug("Creating Company " + compName + " inaugerated: " + inaugurated + " compValue: " + companyValue + " money: " + money + " income: " + income + " performance: " + performance + " pw: " + passwordProtected);
            Company com = new Company(current, compName, inaugurated, companyValue, money, income, performance, passwordProtected);
            companies.add(com);

            /* vehicle info */
            for (int k = 0; k < vehicles.length; k++) {
                com.setNumberOfVehicles(k, Parser.parse16BitNumber(data, i));
                i += 2;
            }
            LOG.debug(com + " has " + Arrays.toString(com.getNumberOfVehicles()) + " vehicles.");

            /* station info */
            for (int k = 0; k < stations.length; k++) {
                com.setNumberOfStations(k, Parser.parse16BitNumber(data, i));
                i += 2;
            }
            LOG.debug(com + " has " + Arrays.toString(com.getNumberOfStations()) + " stations.");
        }
    }

    private void parseVersion4(byte[] data, int activePlayers, int i) {
        LOG.info("Parsing version 4 info.");
        for (int j = 0; j < activePlayers; j++) {
            int current = data[i++];
            int length = Parser.locateNextZero(data, i);
            LOG.debug("New company name seems to be " + length + " characters long.");
            String compName = Parser.parseString(data, i, length);
            i += length + 1;
            int inaugurated = Parser.parse32BitNumber(data, i);
            i += 4;
            long companyValue = Parser.parse64BitNumber(data, i);
            i += 8;
            long money = Parser.parse64BitNumber(data, i);
            i += 8;
            long income = Parser.parse64BitNumber(data, i);
            i += 8;
            int performance = Parser.parse16BitNumber(data, i);
            i += 2;
            boolean passwordProtected = (data[i++] == 1);

            LOG.debug("Creating Company " + compName + " inaugerated: " + inaugurated + " compValue: " + companyValue + " money: " + money + " income: " + income + " performance: " + performance + " pw: " + passwordProtected);
            Company com = new Company(current, compName, inaugurated, companyValue, money, income, performance, passwordProtected);
            companies.add(com);

            /* vehicle info */
            for (int k = 0; k < vehicles.length; k++) {
                com.setNumberOfVehicles(k, Parser.parse16BitNumber(data, i));
                i += 2;
            }
            LOG.debug(com + " has " + Arrays.toString(com.getNumberOfVehicles()) + " vehicles.");

            /* station info */
            for (int k = 0; k < stations.length; k++) {
                com.setNumberOfStations(k, Parser.parse16BitNumber(data, i));
                i += 2;
            }
            LOG.debug(com + " has " + Arrays.toString(com.getNumberOfStations()) + " stations.");

            /* Get a list of clients connected to this company.
             * At this point we read a boolean value from the buffer, if > 0 there is another client
             */
            while (data[i++] > 0) {
                length = Parser.locateNextZero(data, i);
                String cName = Parser.parseString(data, i, length);
                i += length + 1;
                length = Parser.locateNextZero(data, i);
                String uniqueId = Parser.parseString(data, i, length);
                i += length + 1;
                // join_date is transmitted in 
                int[] joinDate = Parser.parseDate(Parser.parse32BitNumber(data, i));
                i += 4;

                Client client = new Client(cName, uniqueId, joinDate, false, com);
                LOG.debug("Found client '" + client + "' connected to company '" + com + "'.");
                LOG.debug("Creating new client: " + cName + " " + uniqueId + " " + Arrays.toString(joinDate));
                com.addClient(client);
                allClients.add(client);
            }
        }

        // Now let's parse any spectators
        while (data[i++] > 0) {
            int length = Parser.locateNextZero(data, i);
            String cName = Parser.parseString(data, i, length);
            // If this is the unreal spectator that is always present on the server.
            if (cName.equals("")) {
                LOG.debug("Ignoring unreal spectator.");
                continue;
            }
            i += length + 1;
            length = Parser.locateNextZero(data, i);
            String uniqueId = Parser.parseString(data, i, length);
            i += length + 1;
            int[] joinDate = Parser.parseDate(Parser.parse32BitNumber(data, i));
            i += 4;

            LOG.debug("Creating new spectator: " + cName + " " + uniqueId + " " + Arrays.toString(joinDate));
            Client client = new Client(cName, uniqueId, joinDate, true, null);
            LOG.debug("Found spectator '" + client + "'.");
            allClients.add(client);
        }
    }

    /**
     * Method for getting all {@link Client}s.
     * @return      an {@link ArrayList} of {@link Client}
     */
    public List<Client> getAllClients() throws JOTLException {
        if (getVersion() == 5) {
            throw new JOTLException("This data is not available in version 5(OpenTTD version 0.6.2).");
        }
        return Collections.unmodifiableList(allClients);
    }

    /**
     * This method is for getting a {@link ArrayList} of all connected
     * players.
     * @return      a copy of the list
     */
    public List<Client> getPlayers() throws JOTLException {
        return getClients(true);
    }

    /**
     * This method is for getting a {@link ArrayList} of all connected
     * spectators.
     * @return      a copy of the list
     */
    public List<Client> getSpectators() throws JOTLException {
        return getClients(false);
    }

    /**
     * This is a support method for getSpectators() and getPlayers().
     * @param player            true to get players, false to get spectators
     * @return                  a copy of the list
     * @see #getPlayers()
     * @see #getSpectators()
     */
    private List<Client> getClients(boolean player) throws JOTLException {
        if (getVersion() == 5) {
            throw new JOTLException("This data is not available in version 5(OpenTTD version 0.6.2).");
        }
        Collections.sort(allClients);
        List<Client> list = new ArrayList<Client>();
        for (Client c : allClients) {
            if (c.isSpectator() != player) {
                list.add(c);
            }
        }
        return list;
    }

    /**
     * This method is for getting a {@link ArrayList} of all companies
     * currently in play.
     * @return      a copy of the list
     */
    public List<Company> getCompanies() {
        Collections.sort(companies);
        return Collections.unmodifiableList(companies);
    }

    /**
     * Method for getting a textual representation of this object.
     * Very useful for debugging.
     * @return      a {@link String} containing all data.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("\tServerDetailedInfo:\n");
        sb.append("\t\tCompanies: \n");
        for (Company com : getCompanies()) {
            sb.append("\t\t\t").append(com).append("\n");
            List<Client> clients = com.getClients();
            if (getVersion() >= 5 && clients.size() > 0) {
                sb.append("\t\t\t\tClients:\n");
                for (Client c : clients) {
                    sb.append("\t\t\t\t\t").append(c).append("\n");
                }
            }
        }

        try {
            List<Client> clients = getSpectators();
            if (getVersion() >= 5 && clients.size() > 0) {
                sb.append("\t\tSpectators:\n");
                for (Client c : clients) {
                    sb.append("\t\t\t").append(c).append("\n");
                }
            }
        } catch (JOTLException ex) {
            // This should not occur
            }
        return sb.toString();
    }
    /** The vehicle types */
    public static String[] vehicles = new String[]{
        "Train", "Truck", "Bus", "Aircraft", "Ship"
    };
    /** The station types */
    public static String[] stations = new String[]{
        "Train Station", "Truck Stop", "Bus Stop", "Airport", "Dock"
    };

    /**
     * Getter for the UDP-packet version.
     * @return      the version
     */
    public int getVersion() {
        return version;
    }
}
