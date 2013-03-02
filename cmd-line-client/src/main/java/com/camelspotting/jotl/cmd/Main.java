package com.camelspotting.jotl.cmd;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.camelspotting.jotl.JOTLException;
import com.camelspotting.jotl.JOTLQuerier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Mats Andreassen
 */
public class Main
{

    private static final Logger LOG = LoggerFactory.getLogger( Main.class );

    private Main()
    {
    }

    public static void main( String... args )
    {
        Params params = new Params();
        JCommander com = new JCommander( params );
        try
        {
            com.parse( args );
            String host = params.host;
            int port = params.port;
            int localPort = params.localPort;

            JOTLQuerier q = new JOTLQuerier( host, localPort, port, true );
            println( q.toString() );
        }
        catch ( ParameterException ex )
        {
            println( String.format( "Parse error: %s", ex.getMessage() ) );
            com.usage();
        }
        catch ( JOTLException ex )
        {
            LOG.error( ex.getMessage(), ex );
            println( ex.getMessage() );
        }
    }

    private static void println( String msg )
    {
        System.out.println( msg );
    }
}
