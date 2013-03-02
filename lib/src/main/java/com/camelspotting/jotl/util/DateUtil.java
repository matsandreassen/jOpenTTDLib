package com.camelspotting.jotl.util;

import java.text.DateFormatSymbols;
import java.util.Locale;

/**
 *
 * @author Mats Andreassen
 */
public class DateUtil
{

    private DateUtil()
    {
    }

    /**
     * Method for getting a date converted to a string with a short month.
     *
     * @param date the date to parse
     * @return the formatted string
     */
    public static String getShortDate( int[] date, Locale loc )
    {
        return new StringBuilder( new DateFormatSymbols( loc ).getShortMonths()[date[1]] ).append( " " ).append( date[0] ).append( " " ).append( date[2] ).toString();
    }

    /**
     * Method for getting a date converted to a string with a long month.
     *
     * @param date the date to parse
     * @return the formatted string
     */
    public static String getLongDate( int[] date, Locale loc )
    {
        return new StringBuilder( new DateFormatSymbols( loc ).getMonths()[date[1]] ).append( " " ).append( date[0] ).append( " " ).append( date[2] ).toString();
    }
}
