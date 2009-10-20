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

/**
 * This class represents a company currently in the game.
 * @author Mats Andreassen
 * @version 1.0
 */
public final class Company implements Comparable<Company> {

    /** In-game id */
    private int currentId;
    /** The company name */
    private String companyName;
    /** The year the company was founded */
    private int inaugerated;
    /** A company's worth */
    private long companyValue;
    /** The current balance */
    private long balance;
    /** The company's income */
    private long income;
    /** Whether or not the company has been password protected */
    private boolean pwProtected;
    /** The rating of a company */
    private int rating;
    /** The vehicle count */
    private int[] vehicles;
    /** The station count */
    private int[] stations;
    /** Clients connected to this company */
    private ArrayList<Client> clients;

    /**
     * The constructor for companies.
     * @param currentId     in-game id
     * @param companyName   the company name
     * @param inaugerated   the year the company was founded
     * @param companyValue  a company's worth
     * @param balance       current balance
     * @param income        company's income
     * @param rating        company's rating
     * @param pwProtected   whether or not the company has been password protected
     */
    Company(int currentId, String companyName, int inaugerated, long companyValue, long balance, long income, int performance, boolean pwProtected) {
        this.currentId = currentId;
        this.companyName = companyName;
        this.inaugerated = inaugerated;
        this.companyValue = companyValue;
        this.balance = balance;
        this.income = income;
        this.rating = performance;
        this.pwProtected = pwProtected;
        this.vehicles = new int[ServerDetailedInfo.vehicles.length];
        this.stations = new int[ServerDetailedInfo.stations.length];
    }

    /**
     * Method for adding a new {@link Client} to this company
     * @param c     the {@link Client} to add
     */
    void addClient(Client c) {
        if (clients == null) {
            clients = new ArrayList<Client>();
        }
        clients.add(c);
    }

    /**
     * Method for accessing the {@link Client}s connected
     * to this company.
     * @return      an {@link ArrayList} containing the {@link Client}s
     */
    public List<Client> getClients() {
        if (clients == null) {
            clients = new ArrayList<Client>();
        }
        return Collections.unmodifiableList(clients);
    }

    /**
     * Method for setting how many vehicles a company has.
     * @param index     the index to modify
     * @param value     the value to set
     * @see ServerDetailedInfo#vehicles
     */
    void setNumberOfVehicles(int index, int value) {
        vehicles[index] = value;
    }

    /**
     * Method for setting how many stations a company has.
     * @param index     the index to modify
     * @param value     the value to set
     * @see ServerDetailedInfo#stations
     */
    void setNumberOfStations(int index, int value) {
        stations[index] = value;
    }

    /**
     * Method for accessing how many vehicles a company has.
     * Match the indices in this array with the 
     * {@link ServerDetailedInfo#vehicles} array to find out
     * what number is what kind of vehicle.
     * @return  the array containing the numbers
     * @see ServerDetailedInfo#vehicles
     */
    public int[] getNumberOfVehicles() {
        int[] A = new int[vehicles.length];
        System.arraycopy(vehicles, 0, A, 0, vehicles.length);
        return A;
    }

    /**
     * Method for accessing how many stations a company has.
     * Match the indices in this array with the 
     * {@link ServerDetailedInfo#stations} array to find out
     * what number is what kind of stations.
     * @return  the array containing the numbers
     * @see ServerDetailedInfo#stations
     */
    public int[] getNumberOfStations() {
        int[] A = new int[stations.length];
        System.arraycopy(stations, 0, A, 0, stations.length);
        return A;
    }

    /**
     * Method for getting the current ID of a company
     * @return  the id
     */
    public int getCurrentID() {
        return currentId;
    }

    /**
     * Method for getting the company's name
     * @return  the name
     */
    public String getCompanyName() {
        return companyName;
    }

    /**
     * Method for getting the company's inaugeration year.
     * @return      the date
     */
    public int getInaugerationYear() {
        return inaugerated;
    }

    /**
     * Method for getting the company's current value.
     * @return      the value
     */
    public long getCompanyValue() {
        return companyValue;
    }

    /**
     * Method for getting the company's current
     * value in a format suitable for display.
     * E.g: £34.241.234
     * @return      the value 
     */
    public String getFormattedCompanyValue() {
        return filterMoney(companyValue);
    }

    /**
     * Method for getting the company's current
     * balance.
     * @return      the balance
     */
    public long getBalance() {
        return balance;
    }

    /**
     * Method for getting the company's current
     * balance in a format suitable for display.
     * E.g: £34.241.234
     * @return      the balance 
     */
    public String getFormattedBalance() {
        return filterMoney(balance);
    }

    /**
     * This support method facilitates money being presented
     * in a practical manner.
     * @param sum       the sum to format
     * @return          the formatted sum
     * @see #getFormattedBalance()
     * @see #getFormattedIncome()
     * @see #getFormattedCompanyValue()
     */
    private String filterMoney(long sum) {
        String out = "";
        char[] ca = Long.toString(sum).toCharArray();
        for (int i = ca.length - 1, ctr = 0; i > -1; i--, ctr++) {
            if (ctr != 0 && ctr % 3 == 0) {
                if (i - 1 >= 0 && ca[i - 1] == '-') {
                    continue;
                } else {
                    out = "." + out;
                }
            }
            out = ca[i] + out;
        }
        return "£" + out;
    }

    /**
     * Method for getting the company's current
     * income.
     * @return      the income
     */
    public long getIncome() {
        return income;
    }

    /**
     * Method for getting the company's current
     * income in a format suitable for display.
     * E.g: £34.241.234
     * @return      the income 
     */
    public String getFormattedIncome() {
        return filterMoney(income);
    }

    /**
     * Method for getting the company's current
     * rating.
     * @return      the rating
     */
    public int getRating() {
        return rating;
    }

    /**
     * Method for finding out whether or not the
     * company is password protected.
     * @return      whether or not the company is password protected
     */
    public boolean isPasswordProtected() {
        return pwProtected;
    }

    /**
     * Convenience method for printing out relevant information
     * for display.
     * @return      a {@link String} representation of the object
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Company(").append(getCurrentID()).append("): rating: ");
        sb.append(rating).append(", name: ").append(companyName).append(", value: ").append(companyValue);
        sb.append(", income: ").append(income).append(", balance: ").append(balance).append(", inaugeration: ");
        sb.append(inaugerated).append(", pw protected: ").append(pwProtected ? "yes" : "no").append(", stations: ");
        sb.append(Arrays.toString(stations)).append(", vehicles: ").append(Arrays.toString(vehicles)).append(".");
        return sb.toString();
    }

    /**
     * This method makes the companies comparable based on their rating.
     * @param o     the {@link Company} to compare to
     * @return      the difference in performances
     */
    public int compareTo(Company o) {
        return o.getRating() - this.rating;
    }

    /**
     * This method returns true if two companies have the same currentID.
     * This returns true if the the objects represent the same company
     * inside one game by checking current ID and inaugeration year.
     * @return          whether they are equal or not
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof Company) {
            Company c = (Company) o;
            return currentId == c.getCurrentID() && c.getInaugerationYear() == getInaugerationYear();
        } else {
            return false;
        }
    }
}
