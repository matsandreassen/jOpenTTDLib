package com.camelspotting.jotl.udp;

/**
 *
 * @author Mats Andreassen
 */
public class PacketMetadata
{

    private final int length;
    private final PacketType type;
    private final int version;

    private PacketMetadata( int length, PacketType type, int version )
    {
        this.length = length;
        this.type = type;
        this.version = version;
    }

    /**
     * The specified length of data packet
     * @return 
     */
    public int getLength()
    {
        return length;
    }

    /**
     * The specified data packet type
     * @return 
     */
    public PacketType getType()
    {
        return type;
    }

    /**
     * The specified UDP version of the data packet
     * @return 
     */
    public int getVersion()
    {
        return version;
    }

    public static PacketMetadata parseMetadata( byte[] input )
    {
        int length = BitUtil.parse8BitNumber( input, 0 );
        if ( length != input.length )
        {
            throw new IllegalArgumentException( String.format( "The specified length %d is different from the actual length %d.", length, input.length ) );
        }


        PacketType type = PacketType.fromInt( BitUtil.parse8BitNumber( input, 2 ) );
        int version = BitUtil.parse8BitNumber( input, 3 );

        return new PacketMetadata( length, type, version );
    }
}
