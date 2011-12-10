package se.l4.aurochs.serialization.format;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.Test;

import com.google.common.base.Charsets;

/**
 * Test for {@link JsonOutput}. Writes a few objects and values and verifies
 * that the returned JSON is correct.
 * 
 * @author Andreas Holstenson
 *
 */
public class JsonOutputTest
{
	@Test
	public void testEmptyObject()
		throws IOException
	{
		StreamingOutput out = createOutput();
		out.writeObjectStart("object");
		out.writeObjectEnd("object");
		
		assertStream(out, "{}");
	}
	
	@Test
	public void testEmptyList()
		throws IOException
	{
		StreamingOutput out = createOutput();
		out.writeListStart("list");
		out.writeListEnd("list");
		
		assertStream(out, "[]");
	}
	
	@Test
	public void testString()
		throws IOException
	{
		StreamingOutput out = createOutput();
		out.write("key", "value");
		
		assertStream(out, "\"value\"");
	}
	
	@Test
	public void testInt()
		throws IOException
	{
		StreamingOutput out = createOutput();
		out.write("key", 12);
		
		assertStream(out, "12");
	}
	
	@Test
	public void testLong()
		throws IOException
	{
		StreamingOutput out = createOutput();
		out.write("key", 12l);
		
		assertStream(out, "12");
	}
	
	@Test
	public void testFloat()
		throws IOException
	{
		StreamingOutput out = createOutput();
		out.write("key", 12.0f);
		
		assertStream(out, "12.0");
	}
	
	@Test
	public void testDouble()
		throws IOException
	{
		StreamingOutput out = createOutput();
		out.write("key", 12.2);
		
		assertStream(out, "12.2");
	}
	
	@Test
	public void testShort()
		throws IOException
	{
		StreamingOutput out = createOutput();
		out.write("key", (short) 12);
		
		assertStream(out, "12");
	}
	
	@Test
	public void testKeyValueString()
		throws IOException
	{
		StreamingOutput out = createOutput();
		out.writeObjectStart("object");
		out.write("key", "value");
		out.writeObjectEnd("object");
		
		assertStream(out, "{\"key\":\"value\"}");
	}
	
	@Test
	public void testKeyValueInt()
		throws IOException
	{
		StreamingOutput out = createOutput();
		out.writeObjectStart("object");
		out.write("key", 12);
		out.writeObjectEnd("object");
		
		assertStream(out, "{\"key\":12}");
	}
	
	@Test
	public void testKeyValueLong()
		throws IOException
	{
		StreamingOutput out = createOutput();
		out.writeObjectStart("object");
		out.write("key", 12l);
		out.writeObjectEnd("object");
		
		assertStream(out, "{\"key\":12}");
	}
	
	@Test
	public void testKeyValueShort()
		throws IOException
	{
		StreamingOutput out = createOutput();
		out.writeObjectStart("object");
		out.write("key", (short) 12);
		out.writeObjectEnd("object");
		
		assertStream(out, "{\"key\":12}");
	}
	
	@Test
	public void testKeyValueFloat()
		throws IOException
	{
		StreamingOutput out = createOutput();
		out.writeObjectStart("object");
		out.write("key", 3.14f);
		out.writeObjectEnd("object");
		
		assertStream(out, "{\"key\":3.14}");
	}
	
	@Test
	public void testKeyValueDouble()
		throws IOException
	{
		StreamingOutput out = createOutput();
		out.writeObjectStart("object");
		out.write("key", 3.14);
		out.writeObjectEnd("object");
		
		assertStream(out, "{\"key\":3.14}");
	}
	
	@Test
	public void testKeyValueObject()
		throws IOException
	{
		StreamingOutput out = createOutput();
		out.writeObjectStart("object");
		out.writeObjectStart("key");
		out.writeObjectEnd("key");
		out.writeObjectEnd("object");
		
		assertStream(out, "{\"key\":{}}");
	}
	
	@Test
	public void testKeyValueList()
		throws IOException
	{
		StreamingOutput out = createOutput();
		out.writeObjectStart("object");
		out.writeListStart("key");
		out.writeListEnd("key");
		out.writeObjectEnd("object");
		
		assertStream(out, "{\"key\":[]}");
	}
	
	@Test
	public void testListString()
		throws IOException
	{
		StreamingOutput out = createOutput();
		out.writeListStart("object");
		out.write("entry", "value");
		out.writeListEnd("object");
		
		assertStream(out, "[\"value\"]");
	}
	
	@Test
	public void testListInt()
		throws IOException
	{
		StreamingOutput out = createOutput();
		out.writeListStart("object");
		out.write("entry", 12);
		out.writeListEnd("object");
		
		assertStream(out, "[12]");
	}
	
	@Test
	public void testListLong()
		throws IOException
	{
		StreamingOutput out = createOutput();
		out.writeListStart("object");
		out.write("entry", 12l);
		out.writeListEnd("object");
		
		assertStream(out, "[12]");
	}
	
	@Test
	public void testListShort()
		throws IOException
	{
		StreamingOutput out = createOutput();
		out.writeListStart("object");
		out.write("entry", (short) 12);
		out.writeListEnd("object");
		
		assertStream(out, "[12]");
	}
	
	@Test
	public void testListFloat()
		throws IOException
	{
		StreamingOutput out = createOutput();
		out.writeListStart("object");
		out.write("entry", 3.14f);
		out.writeListEnd("object");
		
		assertStream(out, "[3.14]");
	}
	
	@Test
	public void testListDouble()
		throws IOException
	{
		StreamingOutput out = createOutput();
		out.writeListStart("object");
		out.write("entry", 3.14);
		out.writeListEnd("object");
		
		assertStream(out, "[3.14]");
	}
	
	@Test
	public void testListMixed()
		throws IOException
	{
		StreamingOutput out = createOutput();
		out.writeListStart("object");
		out.write("entry", 12);
		out.write("entry", "value");
		out.writeListEnd("object");
		
		assertStream(out, "[12,\"value\"]");
	}
	
	@Test
	public void testByteArray()
		throws IOException
	{
		StreamingOutput out = createOutput();
		out.write("", "kaka".getBytes(Charsets.UTF_8));
		
		assertStream(out, "\"a2FrYQ==\"");
	}
	
	private StreamingOutput createOutput()
	{
		return new TestJsonOutput();
	}
	
	private void assertStream(StreamingOutput output, String value)
	{
		((TestJsonOutput) output).verify(value);
	}
	
	private static class TestJsonOutput
		extends JsonOutput
	{
		public TestJsonOutput()
		{
			super(new StringWriter());
		}
		
		public void verify(String expected)
		{
			String value = ((StringWriter) writer).toString();
			assertThat(value, is(expected));
		}
	}
}
