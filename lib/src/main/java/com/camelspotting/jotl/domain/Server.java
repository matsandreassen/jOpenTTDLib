package com.camelspotting.jotl.domain;

import java.net.InetAddress;

/**
 *
 * @author Mats Andreassen
 */
public class Server
{

    private String name;
    private String ipAddress;
    private InetAddress address;

    public Server( String name, String ipAddress, InetAddress address )
    {
        this.name = name;
        this.ipAddress = ipAddress;
        this.address = address;
    }

    public Server( String ipAddress, InetAddress address )
    {
        this( null, ipAddress, address );
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
}
