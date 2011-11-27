package se.l4.aurochs.serialization.collections;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.RandomAccess;

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
public class ListSerializer<T>
	implements Serializer<List<T>>
{
	private final Serializer<T> itemSerializer;

	public ListSerializer(Serializer<T> itemSerializer)
	{
		this.itemSerializer = itemSerializer;
	}

	@Override
	public List<T> read(StreamingInput in)
		throws IOException
	{
		in.next(Token.LIST_START);
		
		List<T> list = new ArrayList<T>();
		while(in.peek() != Token.LIST_END)
		{
			T value = itemSerializer.read(in);
			list.add(value);
		}
		
		in.next(Token.LIST_END);
		
		return list;
	}

	@Override
	public void write(List<T> object, String name, StreamingOutput stream)
		throws IOException
	{
		stream.writeListStart(name);
		
		if(object instanceof RandomAccess)
		{
			for(int i=0, n=object.size(); i<n; i++)
			{
				itemSerializer.write(object.get(i), "item", stream);
			}
		}
		else
		{
			for(T value : object)
			{
				itemSerializer.write(value, "item", stream);
			}
		}
		
		stream.writeListEnd(name);
	}
	
}
