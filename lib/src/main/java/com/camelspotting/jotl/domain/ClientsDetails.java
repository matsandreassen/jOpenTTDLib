package com.camelspotting.jotl.domain;

import java.util.List;

/**
 *
 * @author Mats Andreassen
 */
public interface ClientsDetails
{
    List<Company> getCompanies();
    
    List<Client> getClients();
}
