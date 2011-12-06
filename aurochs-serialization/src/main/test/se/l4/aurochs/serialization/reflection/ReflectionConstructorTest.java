package se.l4.aurochs.serialization.reflection;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Before;
import org.junit.Test;

import se.l4.aurochs.serialization.DefaultSerializerCollection;
import se.l4.aurochs.serialization.Expose;
import se.l4.aurochs.serialization.ReflectionSerializer;
import se.l4.aurochs.serialization.Serializer;
import se.l4.aurochs.serialization.SerializerCollection;
import se.l4.aurochs.serialization.format.JsonInput;
import se.l4.aurochs.serialization.format.JsonOutput;

import com.google.common.base.Throwables;

public class ReflectionConstructorTest
{
	private SerializerCollection collection;

	@Before
	public void beforeTests()
	{
		collection = new DefaultSerializerCollection();
	}
	
	@Test
	public void testDefaultConstructor()
	{
		Serializer<A> serializer = ReflectionSerializer.create(A.class, collection);
		
		A instance = new A();
		instance.field = "test value";
		testSymmetry(serializer, instance);
	}
	
	@Test
	public void testNonDefaultConstructor()
	{
		Serializer<B> serializer = ReflectionSerializer.create(B.class, collection);
		
		B instance = new B("test value");
		testSymmetry(serializer, instance);
	}
	
	public static class A
	{
		@Expose
		private String field;

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((field == null) ? 0 : field.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if(this == obj)
				return true;
			if(obj == null)
				return false;
			if(getClass() != obj.getClass())
				return false;
			A other = (A) obj;
			if(field == null)
			{
				if(other.field != null)
					return false;
			}
			else if(!field.equals(other.field))
				return false;
			return true;
		}
	}
	
	public static class B
	{
		@Expose
		private final String field;
		
		public B(@Expose("field") String field)
		{
			this.field = field;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((field == null) ? 0 : field.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if(this == obj)
				return true;
			if(obj == null)
				return false;
			if(getClass() != obj.getClass())
				return false;
			B other = (B) obj;
			if(field == null)
			{
				if(other.field != null)
					return false;
			}
			else if(!field.equals(other.field))
				return false;
			return true;
		}
	}
	
	private <T> void testSymmetry(Serializer<T> serializer, T instance)
	{
		try
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			JsonOutput jsonOut = new JsonOutput(out);
			serializer.write(instance, "", jsonOut);
			jsonOut.flush();
			
			ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
			JsonInput jsonIn = new JsonInput(new InputStreamReader(in));
			T read = serializer.read(jsonIn);
			
			assertEquals("Deserialized instance does not match", instance, read);
		}
		catch(IOException e)
		{
			throw Throwables.propagate(e);
		}
	}
}
