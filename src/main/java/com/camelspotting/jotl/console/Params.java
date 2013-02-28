package com.camelspotting.jotl.console;

import com.beust.jcommander.Parameter;

/**
 *
 * @author Mats Andreassen
 */
public class Params
{

//    @Parameter( names = { "-v", "--verbose" }, description = "Level of verbosity." )
//    public Integer verbose = 1;
    @Parameter(required=true, names={"-h","--host"}, description = "Host address (DNS name or IP).")
    public String host;
    @Parameter(names={"-p","--port"}, description = "Host port.")
    public int port = 3979;
}
