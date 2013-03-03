package com.camelspotting.jotl.udp;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Mats Andreassen
 */
public class PacketMetadataTest
{

    @Test
    public void testParseMetadata()
    {
        PacketMetadata pm = PacketMetadata.parseMetadata( new byte[]
        {
            4, 0, 1, 4
        } );

        assertEquals( 4, pm.getLength() );
        assertEquals( PacketType.SERVER_RESPONSE, pm.getType() );
        assertEquals( 4, pm.getVersion() );
    }
}
