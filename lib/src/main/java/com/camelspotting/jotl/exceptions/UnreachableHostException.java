package com.camelspotting.jotl.exceptions;

import com.camelspotting.jotl.domain.Server;

/**
 *
 * @author Mats Andreassen
 */
public class UnreachableHostException extends JOTLException
{

    private final Server server;

    public UnreachableHostException( Server server, Throwable cause )
    {
        super( cause );
        this.server = server;
    }

    public Server getServer()
    {
        return server;
    }
}
