package com.camelspotting.jotl;

import com.camelspotting.jotl.domain.Game;
import com.camelspotting.jotl.domain.Server;
import com.camelspotting.jotl.exceptions.JOTLException;

/**
 *
 * @author Mats Andreassen
 */
public interface GameQuerier
{

    /**
     * Method for accessing server details.
     *
     * @return the current {@link ClientsInfo} object or null if no info has
     * been collected
     */
    ServerDetails getServerDetails() throws JOTLException;

    /**
     * Method for accessing details on the connected clients.
     *
     * @return the current {@link ServerInfo} object or null if no info has been
     * collected
     */
    ClientsDetails getClientsDetails() throws JOTLException;

    /**
     * Method for getting all available information
     *
     * @return a wrapper object for all current state of a game
     */
    Game getAllInformation() throws JOTLException;
    
    /**
     * Returns server related information.
     * @return  an object containing server information
     */
    Server getServer();
}
