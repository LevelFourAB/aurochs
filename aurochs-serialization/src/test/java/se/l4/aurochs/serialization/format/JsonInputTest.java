package se.l4.aurochs.serialization.format;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static se.l4.aurochs.serialization.format.StreamingInput.Token.*;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

import com.google.common.base.Charsets;

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
		StreamingInput input = createInput(v);
		assertStream(input, KEY, VALUE);
		
		input = createInput(v);
		assertStreamValues(input, "key", "value");
	}
	
	@Test
	public void testNullValue()
		throws Exception
	{
		String v = "{\"key\": null}";
		StreamingInput input = createInput(v);
		assertStream(input, OBJECT_START, KEY, NULL, OBJECT_END);
		
		input = createInput(v);
		assertStreamValues(input, "key", null);
	}
	
	@Test
	public void testStringQuote()
		throws Exception
	{
		StreamingInput input;
		
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
		StreamingInput input = createInput("{}");
		assertStream(input, OBJECT_START, OBJECT_END);
		
		input = createInput("{ }");
		assertStream(input, OBJECT_START, OBJECT_END);
	}
	
	@Test
	public void testEmptyList()
		throws Exception
	{
		StreamingInput input = createInput("[]");
		assertStream(input, LIST_START, LIST_END);
		
		input = createInput("[ ]");
		assertStream(input, LIST_START, LIST_END);
	}
	
	@Test
	public void testListValue()
		throws Exception
	{
		String v = "[ \"one\" ]";
		StreamingInput input = createInput(v);
		assertStream(input, LIST_START, VALUE, LIST_END);
		
		input = createInput(v);
		assertStreamValues(input, "one");
	}
	
	@Test
	public void testListValues()
		throws Exception
	{
		String v = "[ \"one\", \"two\" ]";
		StreamingInput input = createInput(v);
		assertStream(input, LIST_START, VALUE, VALUE, LIST_END);
		
		input = createInput(v);
		assertStreamValues(input, "one", "two");
	}
	
	@Test
	public void testObjectValue()
		throws Exception
	{
		String v = "{ \"key1\": \"value1\" }";
		StreamingInput input = createInput(v);
		assertStream(input, OBJECT_START, KEY, VALUE, OBJECT_END);
		
		input = createInput(v);
		assertStreamValues(input, "key1", "value1");
	}
	
	@Test
	public void testObjectValues()
		throws Exception
	{
		String v = "{ \"key1\": \"value1\", \"key2\": \"value2\" }";
		StreamingInput input = createInput(v);
		assertStream(input, OBJECT_START, KEY, VALUE, KEY, VALUE, OBJECT_END);
		
		input = createInput(v);
		assertStreamValues(input, "key1", "value1", "key2", "value2");
	}
	
	@Test
	public void testComplexObject()
		throws Exception
	{
		String v = "{ \"key1\": \"value1\", \"key2\": [ \"value2\" ] }";
		StreamingInput input = createInput(v);
		assertStream(input, OBJECT_START, KEY, VALUE, KEY, LIST_START, VALUE, LIST_END, OBJECT_END);
		
		input = createInput(v);
		assertStreamValues(input, "key1", "value1", "key2", "value2");
	}
	
	@Test
	public void testComplexObject2()
		throws Exception
	{
		String v = "{\"languages\": [],\"fields\": {\"test\": {}}}";
		StreamingInput input = createInput(v);
		assertStream(input, OBJECT_START, KEY, LIST_START, LIST_END, KEY, OBJECT_START, KEY, OBJECT_START, OBJECT_END, OBJECT_END, OBJECT_END);
		
		input = createInput(v);
		assertStreamValues(input, "languages", "fields", "test");
	}
	
	@Test
	public void testComplexObject3()
		throws Exception
	{
		String v = "{\"languages\": [],\"fields\": {\"test\": {\"type\": \"token\",\"primary\": true}}}";
		StreamingInput input = createInput(v);
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
		StreamingInput input = createInput(v);
		assertStream(input, OBJECT_START, KEY, VALUE, KEY, VALUE, OBJECT_END); 
		
		input = createInput(v);
		assertStreamValues(input, "languages", false, "fields", "value");
	}
	
	@Test
	public void testComplexSkip()
		throws Exception
	{
		String v = "{\"languages\": false,\"fields\": {\"test\": {\"type\": \"token\",\"primary\": true}}}";
		StreamingInput input = createInput(v);
		
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
		StreamingInput input = createInput(v);
		
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
	
	@Test
	public void testBinary()
		throws Exception
	{
		String v = "\"key\": \"a2FrYQ==\"";
		StreamingInput input = createInput(v);
		input.next(); // key
		input.next(); // value
		
		assertThat(input.getByteArray(), is("kaka".getBytes(Charsets.UTF_8)));
	}
	
	@Test
	public void testLonger()
		throws Exception
	{
		String v = "{\"show_all_inline_media\":false,\"id\":21789286,\"default_profile\":false,\"profile_background_color\":\"161616\",\"profile_image_url\":\"http:\\/\\/a0.twimg.com\\/profile_images\\/1661276913\\/profile.3_normal.jpg\",\"following\":false,\"statuses_count\":514,\"followers_count\":70,\"utc_offset\":null,\"profile_background_image_url\":\"http:\\/\\/a0.twimg.com\\/profile_background_images\\/325347562\\/x24afe3def97fbe3508dfacb66c97493.png\",\"screen_name\":\"aholstenson\",\"name\":\"Andreas Holstenson\",\"profile_link_color\":\"037EC4\",\"profile_background_image_url_https\":\"https:\\/\\/si0.twimg.com\\/profile_background_images\\/325347562\\/x24afe3def97fbe3508dfacb66c97493.png\",\"listed_count\":1,\"url\":\"http:\\/\\/holstenson.se\",\"protected\":false,\"follow_request_sent\":false,\"created_at\":\"Tue Feb 24 19:52:33 +0000 2009\",\"profile_use_background_image\":true,\"verified\":false,\"profile_image_url_https\":\"https:\\/\\/si0.twimg.com\\/profile_images\\/1661276913\\/profile.3_normal.jpg\",\"is_translator\":false,\"profile_text_color\":\"C503C5\",\"description\":\"Tror p\\u00e5 att vi bara n\\u00e5r \",\"notifications\":false,\"time_zone\":null,\"id_str\":\"21789286\",\"default_profile_image\":false,\"location\":\"\",\"profile_sidebar_border_color\":\"D8D8D8\",\"favourites_count\":0,\"contributors_enabled\":false,\"lang\":\"en\",\"geo_enabled\":true,\"friends_count\":212,\"profile_background_tile\":true,\"profile_sidebar_fill_color\":\"FFFFFF\"}";
		StreamingInput input = createInput(v);
		input.next(OBJECT_START);
		while(input.peek() != OBJECT_END)
		{
			switch(input.next())
			{
				case KEY:
					input.skipValue();
			}
		}
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
				case NULL:
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

	protected StreamingInput createInput(String in)
	{
		return new JsonInput(new StringReader(in));
	}
}
