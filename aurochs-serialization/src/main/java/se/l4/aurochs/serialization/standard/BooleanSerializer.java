package se.l4.aurochs.serialization.standard;

import java.io.IOException;

import se.l4.aurochs.serialization.Serializer;
import se.l4.aurochs.serialization.format.StreamingInput;
import se.l4.aurochs.serialization.format.StreamingInput.Token;
import se.l4.aurochs.serialization.format.StreamingOutput;

/**
 * Serializer for {@link Boolean}.
 * 
 * @author Andreas Holstenson
 *
 */
public class BooleanSerializer
	implements Serializer<Boolean>
{

	@Override
	public Boolean read(StreamingInput in)
		throws IOException
	{
		in.next(Token.VALUE);
		if(in.getValue() == null)
		{
			return null;
		}
		return in.getBoolean();
	}

	@Override
	public void write(Boolean object, String name, StreamingOutput stream)
		throws IOException
	{
		stream.write(name, object);
	}

}
