package se.l4.aurochs.serialization.standard;

import java.io.IOException;

import se.l4.aurochs.serialization.Serializer;
import se.l4.aurochs.serialization.SerializerFormatDefinition;
import se.l4.aurochs.serialization.format.StreamingInput;
import se.l4.aurochs.serialization.format.ValueType;
import se.l4.aurochs.serialization.format.StreamingInput.Token;
import se.l4.aurochs.serialization.format.StreamingOutput;
import se.l4.aurochs.serialization.internal.SerializerFormatDefinitionBuilderImpl;

/**
 * Serializer for {@link Short}.
 * 
 * @author Andreas Holstenson
 *
 */
public class ShortSerializer
	implements Serializer<Short>
{
	private final SerializerFormatDefinition formatDefinition;

	public ShortSerializer()
	{
		formatDefinition = SerializerFormatDefinition.forValue(ValueType.SHORT);
	}

	@Override
	public Short read(StreamingInput in)
		throws IOException
	{
		in.next(Token.VALUE);
		return in.getShort();
	}

	@Override
	public void write(Short object, String name, StreamingOutput stream)
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
