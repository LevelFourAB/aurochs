package se.l4.aurochs.serialization.standard;

import java.io.IOException;

import se.l4.aurochs.serialization.Serializer;
import se.l4.aurochs.serialization.format.StreamingInput;
import se.l4.aurochs.serialization.format.StreamingInput.Token;
import se.l4.aurochs.serialization.format.StreamingOutput;

/**
 * Serializer for {@link Float}.
 * 
 * @author Andreas Holstenson
 *
 */
public class FloatSerializer
	implements Serializer<Float>
{

	@Override
	public Float read(StreamingInput in)
		throws IOException
	{
		in.next(Token.VALUE);
		return in.getFloat();
	}

	@Override
	public void write(Float object, String name, StreamingOutput stream)
		throws IOException
	{
		stream.write(name, object);
	}

}
