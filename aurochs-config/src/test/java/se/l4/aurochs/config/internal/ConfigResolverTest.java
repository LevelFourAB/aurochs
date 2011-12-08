package se.l4.aurochs.config.internal;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import se.l4.aurochs.config.internal.streaming.ConfigJsonInput;
import se.l4.aurochs.serialization.format.StreamingInput;

/**
 * Check that the dot resolution in {@link ConfigResolver} works.
 * 
 * @author Andreas Holstenson
 *
 */
public class ConfigResolverTest
{
	@Test
	public void testNoDots()
		throws IOException
	{
		Map<String, Object> data = read("key: value");
		
		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put("key", "value");
		
		assertThat(data, is(expected));
	}
	
	@Test
	public void testDots()
		throws IOException
	{
		Map<String, Object> data = read("key.sub: value");
		
		Map<String, Object> sub = new HashMap<String, Object>();
		sub.put("sub", "value");
		
		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put("key", sub);
		
		assertThat(data, is(expected));
	}
	
	protected Map<String, Object> read(String in)
		throws IOException
	{
		StreamingInput input = createInput(in);
		Map<String, Object> data = RawFormatReader.read(input);
		return ConfigResolver.resolve(data);
	}

	protected StreamingInput createInput(String in)
	{
		return new ConfigJsonInput(new StringReader(in));
	}

}
