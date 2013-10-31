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
 * Serializer for {@link Boolean}.
 * 
 * @author Andreas Holstenson
 *
 */
public class BooleanSerializer
	implements Serializer<Boolean>
{
	private final SerializerFormatDefinition formatDefinition;

	public BooleanSerializer()
	{
		formatDefinition = SerializerFormatDefinition.forValue(ValueType.BOOLEAN);
	}

	@Override
	public Boolean read(StreamingInput in)
		throws IOException
	{
		in.next(Token.VALUE);
		return in.getBoolean();
	}

	@Override
	public void write(Boolean object, String name, StreamingOutput stream)
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
