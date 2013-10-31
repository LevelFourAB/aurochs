package se.l4.aurochs.serialization.standard;

import java.io.IOException;

import se.l4.aurochs.serialization.Serializer;
import se.l4.aurochs.serialization.SerializerFormatDefinition;
import se.l4.aurochs.serialization.format.StreamingInput;
import se.l4.aurochs.serialization.format.StreamingOutput;
import se.l4.aurochs.serialization.format.ValueType;
import se.l4.aurochs.serialization.internal.SerializerFormatDefinitionBuilderImpl;

/**
 * Serializer for {@link String}.
 * 
 * @author Andreas Holstenson
 *
 */
public class StringSerializer
	implements Serializer<String>
{
	private final SerializerFormatDefinition formatDefinition;

	public StringSerializer()
	{
		formatDefinition = SerializerFormatDefinition.forValue(ValueType.STRING);
	}

	@Override
	public String read(StreamingInput in)
		throws IOException
	{
		in.next(StreamingInput.Token.VALUE);
		return in.getString();
	}

	@Override
	public void write(String object, String name, StreamingOutput stream)
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
