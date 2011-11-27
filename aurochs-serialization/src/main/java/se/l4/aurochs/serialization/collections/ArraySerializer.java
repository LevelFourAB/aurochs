package se.l4.aurochs.serialization.collections;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import se.l4.aurochs.serialization.Serializer;
import se.l4.aurochs.serialization.format.StreamingInput;
import se.l4.aurochs.serialization.format.StreamingOutput;
import se.l4.aurochs.serialization.format.StreamingInput.Token;

/**
 * Serializer for arrays.
 * 
 * @author Andreas Holstenson
 *
 */
public class ArraySerializer
	implements Serializer<Object>
{
	private final Class<?> componentType;
	private final Serializer itemSerializer;

	public ArraySerializer(Class<?> componentType, Serializer<?> itemSerializer)
	{
		this.componentType = componentType;
		this.itemSerializer = itemSerializer;
	}
	
	@Override
	public Object read(StreamingInput in)
		throws IOException
	{
		in.next(Token.LIST_START);
		
		List<Object> list = new ArrayList<Object>();
		while(in.peek() != Token.LIST_END)
		{
			Object value = itemSerializer.read(in);
			list.add(value);
		}
		
		in.next(Token.LIST_END);
		
		Object array = Array.newInstance(componentType, list.size());
		for(int i=0, n=list.size(); i<n; i++)
		{
			Array.set(array, i, list.get(i));
		}
		return array;
	}
	
	@Override
	public void write(Object object, String name, StreamingOutput stream)
		throws IOException
	{
		stream.writeListStart(name);
		for(int i=0, n=Array.getLength(object); i<n; i++)
		{
			Object value = Array.get(object, i);
			itemSerializer.write(value, "item", stream);
		}
		stream.writeListEnd(name);
	}
}
