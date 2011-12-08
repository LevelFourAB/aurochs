package se.l4.aurochs.serialization.standard;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

import se.l4.aurochs.serialization.format.StreamingInput;
import se.l4.aurochs.serialization.format.StreamingInput.Token;
import se.l4.aurochs.serialization.format.StreamingOutput;

/**
 * Test for standard serializers that just verify that they call the correct
 * read and write methods on a {@link StreamingInput} and 
 * {@link StreamingOutput}.
 * 
 * @author Andreas Holstenson
 *
 */
public class BasicReadWriteTest
{
	@Test
	public void testReadString()
		throws Exception
	{
		StreamingInput input = mock(StreamingInput.class);
		when(input.next()).thenReturn(Token.VALUE);
		when(input.getString()).thenReturn("value");
		
		StringSerializer serializer = new StringSerializer();
		String value = serializer.read(input);
		
		assertThat(value, is("value"));
	}
	
	@Test
	public void testWriteString()
		throws Exception
	{
		StreamingOutput output = mock(StreamingOutput.class);
		
		StringSerializer serializer = new StringSerializer();
		serializer.write("value", "key", output);
		
		verify(output).write("key", "value");
	}
	
	@Test
	public void testReadLong()
		throws Exception
	{
		StreamingInput input = mock(StreamingInput.class);
		when(input.next()).thenReturn(Token.VALUE);
		when(input.getLong()).thenReturn(12l);
		
		LongSerializer serializer = new LongSerializer();
		Long value = serializer.read(input);
		
		assertThat(value, is(12l));
	}
	
	@Test
	public void testWriteLong()
		throws Exception
	{
		StreamingOutput output = mock(StreamingOutput.class);
		
		LongSerializer serializer = new LongSerializer();
		serializer.write(12l, "key", output);
		
		verify(output).write("key", 12l);
	}
	
	@Test
	public void testReadInt()
		throws Exception
	{
		StreamingInput input = mock(StreamingInput.class);
		when(input.next()).thenReturn(Token.VALUE);
		when(input.getInt()).thenReturn(12);
		
		IntSerializer serializer = new IntSerializer();
		Integer value = serializer.read(input);
		
		assertThat(value, is(12));
	}
	
	@Test
	public void testWriteInt()
		throws Exception
	{
		StreamingOutput output = mock(StreamingOutput.class);
		
		IntSerializer serializer = new IntSerializer();
		serializer.write(12, "key", output);
		
		verify(output).write("key", 12);
	}
	
	@Test
	public void testReadBoolean()
		throws Exception
	{
		StreamingInput input = mock(StreamingInput.class);
		when(input.next()).thenReturn(Token.VALUE);
		when(input.getBoolean()).thenReturn(true);
		
		BooleanSerializer serializer = new BooleanSerializer();
		Boolean value = serializer.read(input);
		
		assertThat(value, is(true));
	}
	
	@Test
	public void testWriteBoolean()
		throws Exception
	{
		StreamingOutput output = mock(StreamingOutput.class);
		
		BooleanSerializer serializer = new BooleanSerializer();
		serializer.write(true, "key", output);
		
		verify(output).write("key", true);
	}
	
	@Test
	public void testReadDouble()
		throws Exception
	{
		StreamingInput input = mock(StreamingInput.class);
		when(input.next()).thenReturn(Token.VALUE);
		when(input.getDouble()).thenReturn(3.14);
		
		DoubleSerializer serializer = new DoubleSerializer();
		Double value = serializer.read(input);
		
		assertThat(value, is(3.14));
	}
	
	@Test
	public void testWriteDouble()
		throws Exception
	{
		StreamingOutput output = mock(StreamingOutput.class);
		
		DoubleSerializer serializer = new DoubleSerializer();
		serializer.write(3.14, "key", output);
		
		verify(output).write("key", 3.14);
	}
	
	@Test
	public void testReadFloat()
		throws Exception
	{
		StreamingInput input = mock(StreamingInput.class);
		when(input.next()).thenReturn(Token.VALUE);
		when(input.getFloat()).thenReturn(7.4f);
		
		FloatSerializer serializer = new FloatSerializer();
		Float value = serializer.read(input);
		
		assertThat(value, is(7.4f));
	}
	
	@Test
	public void testWriteFloat()
		throws Exception
	{
		StreamingOutput output = mock(StreamingOutput.class);
		
		FloatSerializer serializer = new FloatSerializer();
		serializer.write(7.4f, "key", output);
		
		verify(output).write("key", 7.4f);
	}
	
	@Test
	public void testReadShort()
		throws Exception
	{
		StreamingInput input = mock(StreamingInput.class);
		when(input.next()).thenReturn(Token.VALUE);
		when(input.getShort()).thenReturn((short) 12);
		
		ShortSerializer serializer = new ShortSerializer();
		Short value = serializer.read(input);
		
		assertThat(value, is((short) 12));
	}
	
	@Test
	public void testWriteShort()
		throws Exception
	{
		StreamingOutput output = mock(StreamingOutput.class);
		
		ShortSerializer serializer = new ShortSerializer();
		serializer.write((short) 12, "key", output);
		
		verify(output).write("key", (short) 12);
	}
}
