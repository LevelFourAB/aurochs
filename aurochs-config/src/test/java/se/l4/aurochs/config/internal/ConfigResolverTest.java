package se.l4.aurochs.config.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import se.l4.aurochs.config.ConfigKey;
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
		sub.put(ConfigKey.NAME, "key");
		
		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put("key", sub);
		
		assertThat(data, is(expected));
	}
	
	@Test
	public void testDotsInObject()
		throws IOException
	{
		Map<String, Object> data = read("key {\nsub.path: value\n}");
		
		Map<String, Object> path = new HashMap<String, Object>();
		path.put("path", "value");
		path.put(ConfigKey.NAME, "key.sub");
		
		Map<String, Object> sub = new HashMap<String, Object>();
		sub.put("sub", path);
		sub.put(ConfigKey.NAME, "key");
		
		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put("key", sub);
		
		assertThat(data, is(expected));
	}
	
	@Test
	public void testDotsInList()
		throws IOException
	{
		Map<String, Object> data = read("key [\n{\nsub.path: value\n}\n]");
		
		Map<String, Object> path = new HashMap<String, Object>();
		path.put("path", "value");
		path.put(ConfigKey.NAME, "key[0].sub");
		
		Map<String, Object> sub = new HashMap<String, Object>();
		sub.put("sub", path);
		sub.put(ConfigKey.NAME, "key[0]");
		
		List<Object> subList = new ArrayList<Object>();
		subList.add(sub);
		
		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put("key", subList);
		
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
