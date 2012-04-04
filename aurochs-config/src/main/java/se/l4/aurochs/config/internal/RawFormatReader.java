package se.l4.aurochs.config.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.l4.aurochs.config.internal.streaming.ConfigJsonInput;
import se.l4.aurochs.config.internal.streaming.MapInput;
import se.l4.aurochs.serialization.SerializationException;
import se.l4.aurochs.serialization.format.StreamingInput;
import se.l4.aurochs.serialization.format.StreamingInput.Token;

/**
 * Reader of configuration values. Uses {@link ConfigJsonInput} and outputs
 * objects that can be used with {@link MapInput}.
 * 
 * @author Andreas Holstenson
 *
 */
public class RawFormatReader
{
	private RawFormatReader()
	{
	}
	
	/**
	 * Convert the given stream to a map.
	 * 
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	public static Map<String, Object> read(InputStream stream)
		throws IOException
	{
		ConfigJsonInput input = new ConfigJsonInput(new InputStreamReader(stream));
		return read(input);
	}
	
	/**
	 * Convert the given stream to a map.
	 * 
	 * @param input
	 * @return
	 * @throws IOException
	 */
	public static Map<String, Object> read(StreamingInput input)
		throws IOException
	{
		switch(input.peek())
		{
			case LIST_START:
			case LIST_END:
			case OBJECT_END:
			case VALUE:
			case NULL:
				throw new SerializationException("Error in configuration file, should be in in key, value format");
		}
		
		return readMap(input);
	}
	
	/**
	 * Read a single map from the input, optionally while reading object
	 * start and end tokens.
	 * 
	 * @param input
	 * @return
	 * @throws IOException
	 */
	private static Map<String, Object> readMap(StreamingInput input)
		throws IOException
	{
		boolean readEnd = false;
		if(input.peek() == Token.OBJECT_START)
		{
			// Check if the object is wrapped
			readEnd = true;
			input.next();
		}
		
		Token t;
		Map<String, Object> result = new HashMap<String, Object>();
		while((t = input.peek()) != Token.OBJECT_END && t != null)
		{
			// Read the key
			input.next(Token.KEY);
			String key = input.getString();
			
			// Read the value
			Object value = readDynamic(input);
			
			result.put(key, value);
		}
		
		if(readEnd)
		{
			input.next(Token.OBJECT_END);
		}
		
		return result;
	}
	
	/**
	 * Read a list from the input.
	 * 
	 * @param input
	 * @return
	 * @throws IOException
	 */
	private static List<Object> readList(StreamingInput input)
		throws IOException
	{
		input.next(Token.LIST_START);
		
		List<Object> result = new ArrayList<Object>();
		while(input.peek() != Token.LIST_END)
		{
			// Read the value
			Object value = readDynamic(input);
			result.add(value);
		}
		
		input.next(Token.LIST_END);
		
		return result;
	}
	
	/**
	 * Depending on the next token read either a value, a list or a map.
	 * 
	 * @param input
	 * @return
	 * @throws IOException
	 */
	private static Object readDynamic(StreamingInput input)
		throws IOException
	{
		switch(input.peek())
		{
			case VALUE:
				input.next();
				return input.getValue();
			case NULL:
				input.next();
				return input.getValue();
			case LIST_START:
				return readList(input);
			case OBJECT_START:
				return readMap(input);
		}
		
		throw new SerializationException("Unable to read file, unknown start of value: " + input.peek());
	}
}
