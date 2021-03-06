package com.camelspotting.jotl.exceptions;

import com.camelspotting.jotl.domain.Server;

/**
 *
 * @author Mats Andreassen
 */
public class IllegalHostException extends JOTLException
{

    private final Server server;

    public IllegalHostException( Server server, Throwable cause )
    {
        super( cause );
        this.server = server;
    }

    public Server getServer()
    {
        return server;
    }
}
