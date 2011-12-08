package se.l4.aurochs.config.internal;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import se.l4.aurochs.config.internal.streaming.ConfigJsonInput;
import se.l4.aurochs.serialization.format.StreamingInput;

/**
 * Test of {@link RawFormatReader}.
 * 
 * @author Andreas Holstenson
 *
 */
public class ConfigReaderTest
{
	@Test
	public void testWithBraces()
		throws IOException
	{
		StreamingInput in = createInput("{ key: value }");
		Map<String, Object> data = RawFormatReader.read(in);
		
		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put("key", "value");
		
		assertThat(data, is(expected));
	}
	
	@Test
	public void testNoBraces()
		throws IOException
	{
		StreamingInput in = createInput("key: value");
		Map<String, Object> data = RawFormatReader.read(in);
		
		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put("key", "value");
		
		assertThat(data, is(expected));
	}
	
	@Test
	public void testNoBracesList()
		throws IOException
	{
		StreamingInput in = createInput("key: [ value ]");
		Map<String, Object> data = RawFormatReader.read(in);
		
		List<Object> list = new ArrayList<Object>();
		list.add("value");
		
		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put("key", list);
		
		assertThat(data, is(expected));
	}

	protected StreamingInput createInput(String in)
	{
		return new ConfigJsonInput(new StringReader(in));
	}

}
