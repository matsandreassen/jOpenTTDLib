package com.camelspotting.jotl.domain;

import java.net.InetAddress;

/**
 *
 * @author Mats Andreassen
 */
public class Server
{

    private final String name;
    private final String ipAddress;
    private final int port;
    private final InetAddress address;

    public Server( String name, String ipAddress, int port, InetAddress address )
    {
        this.name = name;
        this.ipAddress = ipAddress;
        this.port = port;
        this.address = address;
    }

    public Server( String ipAddress, int port, InetAddress address )
    {
        this( null, ipAddress, port, address );
    }

    public InetAddress getAddress()
    {
        return address;
    }

    public String getIpAddress()
    {
        return ipAddress;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        if ( name != null )
        {
            return String.format( "Server: %s - %s @ %d", name, ipAddress, port );
        }
        else
        {
            return String.format( "Server: %s @ %d", ipAddress, port );
        }
    }
}
