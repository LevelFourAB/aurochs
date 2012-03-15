package se.l4.aurochs.serialization.collections;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import se.l4.aurochs.serialization.Serializer;
import se.l4.aurochs.serialization.format.StreamingInput;
import se.l4.aurochs.serialization.format.StreamingInput.Token;
import se.l4.aurochs.serialization.format.StreamingOutput;

/**
 * Serializer for {@link List}.
 * 
 * @author Andreas Holstenson
 *
 * @param <T>
 */
public class SetSerializer<T>
	implements Serializer<Set<T>>
{
	private final Serializer<T> itemSerializer;

	public SetSerializer(Serializer<T> itemSerializer)
	{
		this.itemSerializer = itemSerializer;
	}

	@Override
	public Set<T> read(StreamingInput in)
		throws IOException
	{
		in.next(Token.LIST_START);
		
		Set<T> list = new HashSet<T>();
		while(in.peek() != Token.LIST_END)
		{
			T value = itemSerializer.read(in);
			list.add(value);
		}
		
		in.next(Token.LIST_END);
		
		return list;
	}

	@Override
	public void write(Set<T> object, String name, StreamingOutput stream)
		throws IOException
	{
		stream.writeListStart(name);
		
		for(T value : object)
		{
			itemSerializer.write(value, "item", stream);
		}
		
		stream.writeListEnd(name);
	}
	
}
