package se.l4.aurochs.serialization.internal.reflection;

import java.io.IOException;

import se.l4.aurochs.serialization.Serializer;
import se.l4.aurochs.serialization.SerializerFormatDefinition;
import se.l4.aurochs.serialization.format.StreamingInput;
import se.l4.aurochs.serialization.format.StreamingInput.Token;
import se.l4.aurochs.serialization.format.StreamingOutput;

/**
 * Serializer that uses only fields or methods. Can fully stream the object.
 * 
 * @author Andreas Holstenson
 *
 * @param <T>
 */
public class ReflectionStreamingSerializer<T>
	implements Serializer<T>
{
	private final TypeInfo<T> type;

	public ReflectionStreamingSerializer(TypeInfo<T> type)
	{
		this.type = type;
	}
	
	@Override
	public T read(StreamingInput in)
		throws IOException
	{
		in.next(Token.OBJECT_START);
		
		T instance = type.newInstance(null);
		while(in.peek() != Token.OBJECT_END)
		{
			in.next(Token.KEY);
			String key = in.getString();
			
			FieldDefinition def = type.getField(key);
			if(def == null)
			{
				// No such field, skip the entire value
				in.skipValue();
			}
			else
			{
				def.read(instance, in);
			}
		}
		
		in.next(Token.OBJECT_END);
		return instance;
	}

	@Override
	public void write(T object, String name, StreamingOutput stream)
		throws IOException
	{
		stream.writeObjectStart(name);
		
		for(FieldDefinition def : type.getAllFields())
		{
			def.write(object, stream);
		}
		
		stream.writeObjectEnd(name);
	}
	
	@Override
	public SerializerFormatDefinition getFormatDefinition()
	{
		return type.getFormatDefinition();
	}
}
