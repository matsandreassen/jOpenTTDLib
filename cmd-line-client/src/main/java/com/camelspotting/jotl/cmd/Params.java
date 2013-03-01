package com.camelspotting.jotl.cmd;

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
    @Parameter(names={"-lp","--local-port"}, description = "The local port to which the local socket is bound.")
    public int localPort = 2222;
}
