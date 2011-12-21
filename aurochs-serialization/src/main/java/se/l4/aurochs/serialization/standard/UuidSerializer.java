package se.l4.aurochs.serialization.standard;

import java.io.IOException;
import java.util.UUID;

import se.l4.aurochs.serialization.Serializer;
import se.l4.aurochs.serialization.format.StreamingInput;
import se.l4.aurochs.serialization.format.StreamingInput.Token;
import se.l4.aurochs.serialization.format.StreamingOutput;

/**
 * Serializer for {@link UUID} that transforms into a byte array.
 * 
 * @author Andreas Holstenson
 *
 */
public class UuidSerializer
	implements Serializer<UUID>
{

	@Override
	public UUID read(StreamingInput in) throws IOException
	{
		in.next(Token.VALUE);
		return fromBytes(in.getByteArray());
	}
	
	@Override
	public void write(UUID object, String name, StreamingOutput stream)
		throws IOException
	{
		stream.write(name, toBytes(object));
	}

	public static UUID fromBytes(byte[] bytes)
	{
		long msb = 0;
		long lsb = 0;
		for(int i=0; i<8; i++)
		{
			msb = (msb << 8) | (bytes[i] & 0xff);
			lsb = (lsb << 8) | (bytes[8 + i] & 0xff);
		}
		
		return new UUID(msb, lsb);
	}
	
	public static byte[] toBytes(UUID uuid)
	{
		long msb = uuid.getMostSignificantBits();
		long lsb = uuid.getLeastSignificantBits();
		
		byte[] buffer = new byte[16];
		for(int i=0; i<8; i++)
		{
			buffer[i] = (byte) (msb >>> 8 * (7 - i));
			buffer[8+i] = (byte) (lsb >>> 8 * (7 - i));
		}
		
		return buffer;
	}
}
