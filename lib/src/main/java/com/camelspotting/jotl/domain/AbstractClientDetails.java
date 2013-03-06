package com.camelspotting.jotl.domain;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author Mats Andreassen
 */
public abstract class AbstractClientDetails implements ClientsDetails
{

    private List<Company> companies;

    public AbstractClientDetails( List<Company> companies )
    {
        Collections.sort( companies );
        this.companies = companies;
    }

    @Override
    public final List<Company> getCompanies()
    {
        return Collections.unmodifiableList( companies );
    }

    /**
     * Method for getting a textual representation of this object. Very useful
     * for debugging.
     *
     * @return a {@link String} containing all data.
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder( "\tClientsDetails:\n" );
        sb.append( "\t\tCompanies: \n" );
        for ( Company com : getCompanies() )
        {
            sb.append( "\t\t\t" ).append( com ).append( "\n" );
        }
        return sb.toString();
    }
}
