package com.camelspotting.jotl.cmd;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.camelspotting.jotl.GameQuerier;
import com.camelspotting.jotl.exceptions.JOTLException;
import com.camelspotting.jotl.udp.UDPGameQuerier;
import com.camelspotting.jotl.domain.Game;
import com.camelspotting.jotl.exceptions.IllegalHostException;
import com.camelspotting.jotl.exceptions.UnreachableHostException;
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

            GameQuerier q = new UDPGameQuerier( host, localPort, port );
            Game game = q.getAllInformation();
            println( game.toString() );
        }
        catch ( ParameterException ex )
        {
            println( "Parse error: %s", ex.getMessage() );
            com.usage();
        }
        catch ( UnreachableHostException ex )
        {
            println( "Could not reach %s. Cause: %s", ex.getServer(), ex.getMessage() );
        }
        catch ( IllegalHostException ex )
        {
            println( "Could not reach %s. Cause: %s", ex.getServer(), ex.getMessage() );
        }
        catch ( JOTLException ex )
        {
            LOG.error( ex.getMessage(), ex );
            println( ex.getMessage() );
        }
    }

    private static void println( String format, Object... params )
    {
        System.out.println( String.format( format, params ) );
    }
}
