package se.l4.aurochs.serialization.standard;

import java.io.IOException;

import se.l4.aurochs.serialization.Serializer;
import se.l4.aurochs.serialization.SerializerFormatDefinition;
import se.l4.aurochs.serialization.format.StreamingInput;
import se.l4.aurochs.serialization.format.StreamingInput.Token;
import se.l4.aurochs.serialization.format.StreamingOutput;
import se.l4.aurochs.serialization.format.ValueType;

/**
 * Serializer for {@link Integer}.
 * 
 * @author Andreas Holstenson
 *
 */
public class IntSerializer
	implements Serializer<Integer>
{
	private final SerializerFormatDefinition formatDefinition;

	public IntSerializer()
	{
		formatDefinition = SerializerFormatDefinition.forValue(ValueType.INTEGER);
	}

	@Override
	public Integer read(StreamingInput in)
		throws IOException
	{
		in.next(Token.VALUE);
		return in.getInt();
	}

	@Override
	public void write(Integer object, String name, StreamingOutput stream)
		throws IOException
	{
		stream.write(name, object);
	}

	@Override
	public SerializerFormatDefinition getFormatDefinition()
	{
		return formatDefinition;
	}
}
