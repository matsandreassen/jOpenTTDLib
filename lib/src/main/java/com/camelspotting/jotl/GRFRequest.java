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

/**
 * This class represents any graphics requests.
 *
 * @author Mats Andreassen
 * @version 1.0
 */
public class GRFRequest
{

    /**
     * The graphic's ID
     */
    private String id;
    /**
     * The graphic's MD5 checksum
     */
    private String md5Check;
    /**
     * The graphics name
     */
    private String name;

    /**
     * Simple constructor.
     *
     * @param id the graphic's ID
     * @param md5Check the graphic's MD5 checksum
     * @param name the graphic's name
     */
    public GRFRequest( String id, String md5Check, String name )
    {
        this.id = id;
        this.md5Check = md5Check;
        this.name = name;
    }

    /**
     * Simpler constructor.
     *
     * @param id the graphic's ID
     * @param md5Check the graphic's MD5 checksum
     */
    public GRFRequest( String id, String md5Check )
    {
        this( id, md5Check, "n/a" );
    }

    /**
     * Getter for the graphic's name
     *
     * @return the graphic's name or 'n/a' if it has not been downloaded
     */
    public String getName()
    {
        return name;
    }

    /**
     * Method for setting the name when the information has become available.
     *
     * @param name the name to set
     */
    void setName( String name )
    {
        this.name = name;
    }

    /**
     * Getter for id.
     *
     * @return the id
     */
    public String getId()
    {
        return id;
    }

    /**
     * Getter for MD5 checksum.
     *
     * @return the checksum
     */
    public String getMD5Checksum()
    {
        return md5Check;
    }
}
