package se.l4.aurochs.serialization.standard;

import java.io.IOException;

import se.l4.aurochs.serialization.Serializer;
import se.l4.aurochs.serialization.format.StreamingInput;
import se.l4.aurochs.serialization.format.StreamingInput.Token;
import se.l4.aurochs.serialization.format.StreamingOutput;

/**
 * Serializer for {@link Integer}.
 * 
 * @author Andreas Holstenson
 *
 */
public class IntSerializer
	implements Serializer<Integer>
{

	@Override
	public Integer read(StreamingInput in)
		throws IOException
	{
		in.next(Token.VALUE);
		if(in.getValue() == null)
		{
			return null;
		}
		
		return in.getInt();
	}

	@Override
	public void write(Integer object, String name, StreamingOutput stream)
		throws IOException
	{
		stream.write(name, object);
	}

}
