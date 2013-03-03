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
            if ( this instanceof ClientsDetailsV4 )
            {
                ClientsDetailsV4 v4 = (ClientsDetailsV4) this;
                List<Client> clients = v4.getPlayers();
                if ( clients.size() > 0 )
                {
                    sb.append( "\t\t\t\tClients:\n" );
                    for ( Client c : clients )
                    {
                        sb.append( "\t\t\t\t\t" ).append( c ).append( "\n" );
                    }
                }
            }
        }

        if ( this instanceof ClientsDetailsV4 )
        {
            ClientsDetailsV4 v4 = (ClientsDetailsV4) this;
            List<Client> clients = v4.getSpectators();
            if ( clients.size() > 0 )
            {
                sb.append( "\t\tSpectators:\n" );
                for ( Client c : clients )
                {
                    sb.append( "\t\t\t" ).append( c ).append( "\n" );
                }
            }
        }
        return sb.toString();
    }
}
