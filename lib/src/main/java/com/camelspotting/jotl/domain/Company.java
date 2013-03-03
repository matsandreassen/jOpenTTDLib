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
package com.camelspotting.jotl.domain;

import com.camelspotting.jotl.parsing.Station;
import com.camelspotting.jotl.parsing.Vehicle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a company currently in the game.
 *
 * @author Mats Andreassen
 * @version 1.0
 */
public class Company implements Comparable<Company>
{

    /**
     * In-game id
     */
    private final int currentId;
    /**
     * The company name
     */
    private final String companyName;
    /**
     * The year the company was founded
     */
    private final int inaugerated;
    /**
     * A company's worth
     */
    private final long companyValue;
    /**
     * The current balance
     */
    private final long balance;
    /**
     * The company's income
     */
    private final long income;
    /**
     * Whether or not the company has been password protected
     */
    private final boolean pwProtected;
    /**
     * The rating of a company
     */
    private final int rating;
    /**
     * The vehicle count
     */
    private final Map<Vehicle, Integer> vehicleCountMap;
    /**
     * The station count
     */
    private final Map<Station, Integer> stationCountMap;
    /**
     * Clients connected to this company
     */
    private final ArrayList<Client> clients;

    /**
     * The constructor for companies.
     *
     * @param currentId in-game id
     * @param companyName the company name
     * @param inaugerated the year the company was founded
     * @param companyValue a company's worth
     * @param balance current balance
     * @param income company's income
     * @param rating company's rating
     * @param pwProtected whether or not the company has been password protected
     */
    public Company( int currentId, String companyName, int inaugerated, long companyValue, long balance, long income, int performance, boolean pwProtected, Map<Vehicle, Integer> vehicleCountMap, Map<Station, Integer> stationCountMap )
    {
        this.clients = new ArrayList<Client>();
        this.currentId = currentId;
        this.companyName = companyName;
        this.inaugerated = inaugerated;
        this.companyValue = companyValue;
        this.balance = balance;
        this.income = income;
        this.rating = performance;
        this.pwProtected = pwProtected;
        this.vehicleCountMap = vehicleCountMap;
        this.stationCountMap = stationCountMap;
    }

    /**
     * Method for adding a new {@link Client} to this company
     *
     * @param c the {@link Client} to add
     */
    public void addClient( Client c )
    {
        clients.add( c );
    }

    /**
     * Method for accessing the {@link Client}s connected to this company.
     *
     * @return an {@link ArrayList} containing the {@link Client}s
     */
    public List<Client> getClients()
    {
        return Collections.unmodifiableList( clients );
    }

    /**
     * Method for accessing how many vehicles a company has. Match the indices
     * in this array with the {@link ServerInfo#vehicles} array to find out what
     * number is what kind of vehicle.
     *
     * @return the array containing the numbers
     * @see ServerInfo#vehicles
     */
    public Map<Vehicle, Integer> getNumberOfVehicles()
    {
        return new EnumMap<Vehicle, Integer>( vehicleCountMap );
    }

    /**
     * Method for accessing how many stations a company has. Match the indices
     * in this array with the {@link ServerInfo#stations} array to find out what
     * number is what kind of stations.
     *
     * @return the array containing the numbers
     * @see ServerInfo#stations
     */
    public Map<Station, Integer> getNumberOfStations()
    {
        return new EnumMap<Station, Integer>( stationCountMap );
    }

    /**
     * Method for getting the current ID of a company
     *
     * @return the id
     */
    public int getCurrentId()
    {
        return currentId;
    }

    /**
     * Method for getting the company's name
     *
     * @return the name
     */
    public String getCompanyName()
    {
        return companyName;
    }

    /**
     * Method for getting the company's inaugeration year.
     *
     * @return the date
     */
    public int getInaugerationYear()
    {
        return inaugerated;
    }

    /**
     * Method for getting the company's current value.
     *
     * @return the value
     */
    public long getCompanyValue()
    {
        return companyValue;
    }

    /**
     * Method for getting the company's current value in a format suitable for
     * display. E.g: £34.241.234
     *
     * @return the value
     */
    public String getFormattedCompanyValue()
    {
        return filterMoney( companyValue );
    }

    /**
     * Method for getting the company's current balance.
     *
     * @return the balance
     */
    public long getBalance()
    {
        return balance;
    }

    /**
     * Method for getting the company's current balance in a format suitable for
     * display. E.g: £34.241.234
     *
     * @return the balance
     */
    public String getFormattedBalance()
    {
        return filterMoney( balance );
    }

    /**
     * This support method facilitates money being presented in a practical
     * manner.
     *
     * @param sum the sum to format
     * @return the formatted sum
     * @see #getFormattedBalance()
     * @see #getFormattedIncome()
     * @see #getFormattedCompanyValue()
     */
    private String filterMoney( long sum )
    {
        String out = "";
        char[] ca = Long.toString( sum ).toCharArray();
        for ( int i = ca.length - 1, ctr = 0; i > -1; i--, ctr++ )
        {
            if ( ctr != 0 && ctr % 3 == 0 )
            {
                if ( i - 1 >= 0 && ca[i - 1] == '-' )
                {
                    continue;
                }
                else
                {
                    out = "." + out;
                }
            }
            out = ca[i] + out;
        }
        return "£" + out;
    }

    /**
     * Method for getting the company's current income.
     *
     * @return the income
     */
    public long getIncome()
    {
        return income;
    }

    /**
     * Method for getting the company's current income in a format suitable for
     * display. E.g: £34.241.234
     *
     * @return the income
     */
    public String getFormattedIncome()
    {
        return filterMoney( income );
    }

    /**
     * Method for getting the company's current rating.
     *
     * @return the rating
     */
    public int getRating()
    {
        return rating;
    }

    /**
     * Method for finding out whether or not the company is password protected.
     *
     * @return whether or not the company is password protected
     */
    public boolean isPasswordProtected()
    {
        return pwProtected;
    }

    /**
     * Convenience method for printing out relevant information for display.
     *
     * @return a {@link String} representation of the object
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder( "Company #" ).append( currentId ).append( ": rating: " );
        sb.append( rating ).append( ", name: " ).append( companyName ).append( ", value: " ).append( companyValue );
        sb.append( ", income: " ).append( income ).append( ", balance: " ).append( balance ).append( ", inaugeration: " );
        sb.append( inaugerated ).append( ", pw protected: " ).append( pwProtected ? "yes" : "no" ).append( ", stations: " );
        sb.append( stationCountMap.values() ).append( ", vehicles: " ).append( vehicleCountMap.values() ).append( "." );
        return sb.toString();
    }

    /**
     * This method makes the companies comparable based on their rating.
     *
     * @param o the {@link Company} to compare to
     * @return the difference in performances
     */
    @Override
    public int compareTo( Company o )
    {
        return o.getRating() - this.rating;
    }

    /**
     * This method returns true if two companies have the same currentID. This
     * returns true if the the objects represent the same company inside one
     * game by checking current ID and inaugeration year.
     *
     * @return whether they are equal or not
     */
    @Override
    public boolean equals( Object o )
    {
        if ( o instanceof Company )
        {
            Company c = (Company) o;
            return currentId == c.getCurrentId() && c.getInaugerationYear() == getInaugerationYear();
        }
        else
        {
            return false;
        }
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 71 * hash + this.currentId;
        hash = 71 * hash + this.inaugerated;
        return hash;
    }
}
