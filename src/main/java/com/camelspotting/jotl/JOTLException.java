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
 * All exceptions that happen in the code are wrapped inside OpenTTD-exceptions.
 *
 * @author Mats Andressen
 * @version 1.0
 */
public final class JOTLException extends Exception
{

    /**
     * Main constructor for these exceptions.
     *
     * @param msg the message to include.
     */
    public JOTLException( String msg )
    {
        super( msg );
    }

    /**
     * Simple constructor with no message.
     */
    public JOTLException()
    {
        super();
    }

    /**
     * Main constructor for these exceptions.
     *
     * @param msg the message to include.
     * @param cause the cause of this exception
     */
    public JOTLException( String msg, Throwable cause )
    {
        super( msg, cause );
    }
}
