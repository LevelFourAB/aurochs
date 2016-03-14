package se.l4.aurochs.serialization.standard;

import java.io.IOException;

import se.l4.aurochs.serialization.SerializationException;
import se.l4.aurochs.serialization.Serializer;
import se.l4.aurochs.serialization.format.StreamingInput;
import se.l4.aurochs.serialization.format.StreamingInput.Token;
import se.l4.aurochs.serialization.format.StreamingOutput;

/**
 * Serializer for {@link Number}, {@link Boolean} or {@link String}.
 * 
 * @author Andreas Holstenson
 *
 */
public class SimpleTypeSerializer
	implements Serializer<Object>
{

	@Override
	public Object read(StreamingInput in)
		throws IOException
	{
		in.next(Token.VALUE);
		return in.getValue();
	}

	@Override
	public void write(Object object, String name, StreamingOutput stream)
		throws IOException
	{
		if(object instanceof Byte)
		{
			stream.write(name, ((Byte) object).intValue());
		}
		else if(object instanceof Integer)
		{
			stream.write(name, (Integer) object);
		}
		else if(object instanceof Long)
		{
			stream.write(name, (Long) object);
		}
		else if(object instanceof Float)
		{
			stream.write(name, (Float) object);
		}
		else if(object instanceof Double)
		{
			stream.write(name, (Double) object);
		}
		else if(object instanceof Boolean)
		{
			stream.write(name, (Boolean) object);
		}
		else if(object instanceof String)
		{
			stream.write(name, (String) object);
		}
		else
		{
			throw new SerializationException("Can't serialize the given object: " + object);
		}
	}

}
