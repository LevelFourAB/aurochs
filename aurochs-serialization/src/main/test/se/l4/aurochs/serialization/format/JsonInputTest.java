package se.l4.aurochs.serialization.format;

import static junit.framework.Assert.*;
import static se.l4.aurochs.serialization.format.StreamingInput.Token.*;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

import se.l4.aurochs.serialization.format.JsonInput;
import se.l4.aurochs.serialization.format.StreamingInput;

/**
 * Test for {@link JsonInput}. Runs a set of JSON documents and makes sure
 * that we return the correct tokens.
 * 
 * @author Andreas Holstenson
 *
 */
public class JsonInputTest
{

	@Test
	public void testKeyValue()
		throws Exception
	{
		String v = "\"key\": \"value\"";
		JsonInput input = createInput(v);
		assertStream(input, KEY, VALUE);
		
		input = createInput(v);
		assertStreamValues(input, "key", "value");
	}
	
	@Test
	public void testStringQuote()
		throws Exception
	{
		JsonInput input;
		
		input = createInput("\"key\": \"va\\\"lue\"");
		assertStreamValues(input, "key", "va\"lue");
		
		input = createInput("\"key\": \"va\\nlue\"");
		assertStreamValues(input, "key", "va\nlue");
		
		input = createInput("\"key\": \"va\\u4580lue\"");
		assertStreamValues(input, "key", "va\u4580lue");
	}
	
	@Test
	public void testEmptyObject()
		throws Exception
	{
		JsonInput input = createInput("{}");
		assertStream(input, OBJECT_START, OBJECT_END);
		
		input = createInput("{ }");
		assertStream(input, OBJECT_START, OBJECT_END);
	}
	
	@Test
	public void testEmptyList()
		throws Exception
	{
		JsonInput input = createInput("[]");
		assertStream(input, LIST_START, LIST_END);
		
		input = createInput("[ ]");
		assertStream(input, LIST_START, LIST_END);
	}
	
	@Test
	public void testListValue()
		throws Exception
	{
		String v = "[ \"one\" ]";
		JsonInput input = createInput(v);
		assertStream(input, LIST_START, VALUE, LIST_END);
		
		input = createInput(v);
		assertStreamValues(input, "one");
	}
	
	@Test
	public void testListValues()
		throws Exception
	{
		String v = "[ \"one\", \"two\" ]";
		JsonInput input = createInput(v);
		assertStream(input, LIST_START, VALUE, VALUE, LIST_END);
		
		input = createInput(v);
		assertStreamValues(input, "one", "two");
	}
	
	@Test
	public void testObjectValue()
		throws Exception
	{
		String v = "{ \"key1\": \"value1\" }";
		JsonInput input = createInput(v);
		assertStream(input, OBJECT_START, KEY, VALUE, OBJECT_END);
		
		input = createInput(v);
		assertStreamValues(input, "key1", "value1");
	}
	
	@Test
	public void testObjectValues()
		throws Exception
	{
		String v = "{ \"key1\": \"value1\", \"key2\": \"value2\" }";
		JsonInput input = createInput(v);
		assertStream(input, OBJECT_START, KEY, VALUE, KEY, VALUE, OBJECT_END);
		
		input = createInput(v);
		assertStreamValues(input, "key1", "value1", "key2", "value2");
	}
	
	@Test
	public void testComplexObject()
		throws Exception
	{
		String v = "{ \"key1\": \"value1\", \"key2\": [ \"value2\" ] }";
		JsonInput input = createInput(v);
		assertStream(input, OBJECT_START, KEY, VALUE, KEY, LIST_START, VALUE, LIST_END, OBJECT_END);
		
		input = createInput(v);
		assertStreamValues(input, "key1", "value1", "key2", "value2");
	}
	
	@Test
	public void testComplexObject2()
		throws Exception
	{
		String v = "{\"languages\": [],\"fields\": {\"test\": {}}}";
		JsonInput input = createInput(v);
		assertStream(input, OBJECT_START, KEY, LIST_START, LIST_END, KEY, OBJECT_START, KEY, OBJECT_START, OBJECT_END, OBJECT_END, OBJECT_END);
		
		input = createInput(v);
		assertStreamValues(input, "languages", "fields", "test");
	}
	
	@Test
	public void testComplexObject3()
		throws Exception
	{
		String v = "{\"languages\": [],\"fields\": {\"test\": {\"type\": \"token\",\"primary\": true}}}";
		JsonInput input = createInput(v);
		assertStream(input, OBJECT_START, KEY, LIST_START, LIST_END, KEY, OBJECT_START, KEY, 
			OBJECT_START,
				KEY, VALUE, // type = token
				KEY, VALUE, // primary = true
			OBJECT_END, OBJECT_END, OBJECT_END
		);
		
		input = createInput(v);
		assertStreamValues(input, "languages", "fields", "test", "type", "token", "primary", true);
	}
	
	@Test
	public void testKeyAfterBoolean()
		throws Exception
	{
		String v = "{\"languages\": false,\"fields\": \"value\"}";
		JsonInput input = createInput(v);
		assertStream(input, OBJECT_START, KEY, VALUE, KEY, VALUE, OBJECT_END); 
		
		input = createInput(v);
		assertStreamValues(input, "languages", false, "fields", "value");
	}
	
	@Test
	public void testComplexSkip()
		throws Exception
	{
		String v = "{\"languages\": false,\"fields\": {\"test\": {\"type\": \"token\",\"primary\": true}}}";
		JsonInput input = createInput(v);
		
		// Fast forward
		input.next(OBJECT_START);
		input.next(KEY);
		input.next(VALUE);
		
		// Read fields key
		input.next(KEY);
		input.skipValue();
		
		// Read Object end
		input.next(OBJECT_END);
		
		if(input.peek() != null)
		{
			fail("Found " + input.peek() + " in the stream, stream should be empty");
		}
	}
	
	
	@Test
	public void testComplexReading()
		throws Exception
	{
		String v = "{ \"key1\": \"value1\", \"key2\": [ \"value2\" ], \"key3\": \"value3\" }";
		JsonInput input = createInput(v);
		
		boolean ended = false;
		input.next(OBJECT_START);
		while(input.peek() != null)
		{
			switch(input.next())
			{
				case OBJECT_END:
					ended = true;
					break;
				case KEY:
					String key = input.getString();
					if(key.equals("key1"))
					{
						input.next();
						assertEquals("value1", input.getValue());
					}
					else if(key.equals("key3"))
					{
						input.next();
						assertEquals("value3", input.getValue());
					}
					else
					{
						input.skipValue();
					}
			}
		}
		
		if(! ended) fail("Did not read of the object");
	}

	/**
	 * Assert that the stream contains the specified tokens.
	 * 
	 * @param in
	 * @param tokens
	 * @throws IOException
	 */
	private void assertStream(StreamingInput in, StreamingInput.Token... tokens)
		throws IOException
	{
		int i = 0;
		while(in.peek() != null)
		{
			StreamingInput.Token t = in.next();
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
			fail("Did not read all tokens, expected " + tokens.length + " but only read " + i);
		}
	}
	
	/**
	 * Assert that KEY and VALUE tokens contain the specified values.
	 * 
	 * @param in
	 * @param values
	 * @throws IOException
	 */
	private void assertStreamValues(StreamingInput in, Object... values)
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

	private JsonInput createInput(String in)
	{
		return new JsonInput(new StringReader(in));
	}
}
