package se.l4.aurochs.serialization.enums;

import java.io.IOException;

import se.l4.aurochs.serialization.Serializer;
import se.l4.aurochs.serialization.SerializerFormatDefinition;
import se.l4.aurochs.serialization.format.StreamingInput;
import se.l4.aurochs.serialization.format.StreamingInput.Token;
import se.l4.aurochs.serialization.format.StreamingOutput;
import se.l4.aurochs.serialization.internal.SerializerFormatDefinitionBuilderImpl;

/**
 * Serializer for {@link Enum}s. The enum serializer can use different
 * {@link ValueTranslator}s to encode enums in different ways.
 * 
 * @author Andreas Holstenson
 *
 * @param <T>
 */
public class EnumSerializer<T extends Enum<T>>
	implements Serializer<T>
{
	private final ValueTranslator translator;
	private final SerializerFormatDefinition formatDefinition;

	public EnumSerializer(ValueTranslator translator)
	{
		this.translator = translator;
		
		formatDefinition = new SerializerFormatDefinitionBuilderImpl()
			.value(translator.getType())
			.build();
	}
	
	@Override
	public T read(StreamingInput in)
		throws IOException
	{
		in.next(Token.VALUE);
		
		Object value;
		switch(translator.getType())
		{
			case BOOLEAN:
				value = in.getBoolean();
				break;
			case DOUBLE:
				value = in.getDouble();
				break;
			case FLOAT:
				value = in.getFloat();
				break;
			case INTEGER:
				value = in.getInt();
				break;
			case LONG:
				value = in.getLong();
				break;
			case SHORT:
				value = in.getShort();
				break;
			case STRING:
				value = in.getString();
				break;
			default:
				throw new AssertionError("Unknown type: " + translator.getType());
		}
		
		return (T) translator.toEnum(value);
	}
	
	@Override
	public void write(T object, String name, StreamingOutput stream)
		throws IOException
	{
		Object value = translator.fromEnum(object);
		switch(translator.getType())
		{
			case BOOLEAN:
				stream.write(name, (Boolean) value);
				break;
			case DOUBLE:
				stream.write(name, (Double) value);
				break;
			case FLOAT:
				stream.write(name, (Float) value);
				break;
			case INTEGER:
				stream.write(name, (Integer) value);
				break;
			case LONG:
				stream.write(name, (Long) value);
				break;
			case SHORT:
				stream.write(name, (Short) value);
				break;
			case STRING:
				stream.write(name, (String) value);
				break;
			default:
				throw new AssertionError("Unknown type: " + translator.getType());
		}
	}
	
	@Override
	public SerializerFormatDefinition getFormatDefinition()
	{
		return formatDefinition;
	}
}
