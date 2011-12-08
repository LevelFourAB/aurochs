package se.l4.aurochs.config.internal;

import static junit.framework.Assert.*;
import static se.l4.aurochs.serialization.format.StreamingInput.Token.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import se.l4.aurochs.config.internal.streaming.MapInput;
import se.l4.aurochs.serialization.format.StreamingInput;

/**
 * Tests for {@link MapInput}.
 * 
 * @author Andreas Holstenson
 *
 */
public class ObjectStreamsTest
{
	@Test
	public void testSingleString()
		throws Exception
	{
		Map<String, Object> data = createMap();
		data.put("key", "value");
		
		assertStream(createInput(data), OBJECT_START, KEY, VALUE, OBJECT_END);
		assertStreamValues(createInput(data), "key", "value");
	}
	
	@Test
	public void testSingleNumber()
		throws Exception
	{
		Map<String, Object> data = createMap();
		data.put("key", 12.0);
		
		assertStream(createInput(data), OBJECT_START, KEY, VALUE, OBJECT_END);
		assertStreamValues(createInput(data), "key", 12.0);
	}
	
	@Test
	public void testMultipleValues()
		throws Exception
	{
		Map<String, Object> data = createMap();
		data.put("key1", "value1");
		data.put("key2", "value2");
		
		assertStream(createInput(data), OBJECT_START, KEY, VALUE, KEY, VALUE, OBJECT_END);
		assertStreamValues(createInput(data), "key1", "value1", "key2", "value2");
	}
	
	@Test
	public void testList()
		throws Exception
	{
		List<Object> data = new ArrayList<Object>();
		data.add("value1");
		data.add("value2");
		
		assertStream(createInput(data), LIST_START, VALUE, VALUE, LIST_END);
		assertStreamValues(createInput(data), "value1", "value2");
	}
	
	@Test
	public void testListInMap()
		throws Exception
	{
		List<Object> list = new ArrayList<Object>();
		list.add("value1");
		list.add("value2");
		
		Map<String, Object> data = createMap();
		data.put("key1", list);
		
		
		assertStream(createInput(data), OBJECT_START, KEY, LIST_START, VALUE, VALUE, LIST_END, OBJECT_END);
		assertStreamValues(createInput(data), "key1", "value1", "value2");
	}
	
	/**
	 * Assert that the stream contains the specified tokens.
	 * 
	 * @param in
	 * @param tokens
	 * @throws IOException
	 */
	protected void assertStream(StreamingInput in, StreamingInput.Token... tokens)
		throws IOException
	{
		int i = 0;
		List<StreamingInput.Token> history = new ArrayList<StreamingInput.Token>();
		while(in.peek() != null)
		{
			StreamingInput.Token t = in.next();
			history.add(t);
			if(i == tokens.length)
			{
				fail("Did not expect more tokens, but got " + t);
			}
			else if(t != tokens[i++])
			{
				fail("Token at " + (i-1) + " was expected to be " + tokens[i-1] + ", but found " + t);
			}
		}
		
		if(i < tokens.length)
		{
			fail("Did not read all tokens, expected " + tokens.length + " but only read " + i + ".\nTokens were " + history.toString());
		}
	}
	
	/**
	 * Assert that KEY and VALUE tokens contain the specified values.
	 * 
	 * @param in
	 * @param values
	 * @throws IOException
	 */
	protected void assertStreamValues(StreamingInput in, Object... values)
		throws IOException
	{
		int i = 0;
		while(in.peek() != null)
		{
			StreamingInput.Token t = in.next();
			switch(t)
			{
				case KEY:
				case VALUE:
					if(i == values.length)
					{
						fail("Did not expect more values, but got " + in.getValue());
					}
					
					assertEquals(values[i++], in.getValue());
					break;
			}
		}
		
		if(i < values.length)
		{
			fail("Did not read all values, expected " + values.length + " but only read " + i);
		}
	}

	protected StreamingInput createInput(Object in)
	{
		return MapInput.resolveInput(in);
	}
	
	protected Map<String, Object> createMap()
	{
		return new LinkedHashMap<String, Object>();
	}
}
