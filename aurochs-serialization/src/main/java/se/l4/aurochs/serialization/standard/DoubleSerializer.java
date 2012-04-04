package se.l4.aurochs.serialization.standard;

import java.io.IOException;

import se.l4.aurochs.serialization.Serializer;
import se.l4.aurochs.serialization.format.StreamingInput;
import se.l4.aurochs.serialization.format.StreamingInput.Token;
import se.l4.aurochs.serialization.format.StreamingOutput;

/**
 * Serializer for {@link Double}.
 * 
 * @author Andreas Holstenson
 *
 */
public class DoubleSerializer
	implements Serializer<Double>
{

	@Override
	public Double read(StreamingInput in)
		throws IOException
	{
		in.next(Token.VALUE);
		return in.getDouble();
	}

	@Override
	public void write(Double object, String name, StreamingOutput stream)
		throws IOException
	{
		stream.write(name, object);
	}

}
