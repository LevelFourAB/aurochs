package se.l4.aurochs.serialization.standard;

import java.io.IOException;

import se.l4.aurochs.serialization.Serializer;
import se.l4.aurochs.serialization.format.StreamingInput;
import se.l4.aurochs.serialization.format.StreamingInput.Token;
import se.l4.aurochs.serialization.format.StreamingOutput;

/**
 * Serializer for byte arrays as they have special meaning in 
 * {@link StreamingInput} and {@link StreamingOutput}.
 * 
 * @author Andreas Holstenson
 *
 */
public class ByteArraySerializer
	implements Serializer<byte[]>
{

	@Override
	public byte[] read(StreamingInput in)
		throws IOException
	{
		in.next(Token.VALUE);
		return in.getByteArray();
	}

	@Override
	public void write(byte[] object, String name, StreamingOutput stream)
		throws IOException
	{
		stream.write(name, object);
	}

}
