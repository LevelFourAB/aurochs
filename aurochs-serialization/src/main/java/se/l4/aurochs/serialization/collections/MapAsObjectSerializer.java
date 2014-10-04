package se.l4.aurochs.serialization.collections;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import se.l4.aurochs.serialization.Serializer;
import se.l4.aurochs.serialization.SerializerFormatDefinition;
import se.l4.aurochs.serialization.format.StreamingInput;
import se.l4.aurochs.serialization.format.StreamingInput.Token;
import se.l4.aurochs.serialization.format.StreamingOutput;

public class MapAsObjectSerializer<V>
	implements Serializer<Map<String, V>>
{
	private final Serializer<V> serializer;
	private final SerializerFormatDefinition formatDefinition;

	public MapAsObjectSerializer(Serializer<V> serializer)
	{
		this.serializer = serializer;
		
		formatDefinition = SerializerFormatDefinition.builder()
			.field("*").using(serializer)
			.build();
	}
	
	@Override
	public Map<String, V> read(StreamingInput in)
		throws IOException
	{
		in.next(Token.OBJECT_START);
		
		Map<String, V> result = new HashMap<String, V>();
		while(in.peek() != Token.OBJECT_END)
		{
			in.next(Token.KEY);
			String key = in.getString();
			if(key.startsWith("_aurochs_:"))
			{
				in.skipValue();
				continue;
			}
			
			V value = in.peek() == Token.NULL ? null : serializer.read(in);
			
			result.put(key, value);
		}
		
		in.next(Token.OBJECT_END);
		
		return result;
	}
	
	@Override
	public void write(Map<String, V> object, String name, StreamingOutput stream)
		throws IOException
	{
		stream.writeObjectStart(name);
		
		for(Entry<String, V> e : object.entrySet())
		{
			V value = e.getValue();
			if(value == null)
			{
				stream.writeNull(name);
			}
			else
			{
				serializer.write(value, e.getKey(), stream);
			}
		}
		
		stream.writeObjectEnd(name);
	}
	
	@Override
	public SerializerFormatDefinition getFormatDefinition()
	{
		return formatDefinition;
	}
}
