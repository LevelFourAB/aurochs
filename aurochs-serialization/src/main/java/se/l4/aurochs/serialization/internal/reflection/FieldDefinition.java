package se.l4.aurochs.serialization.internal.reflection;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import se.l4.aurochs.serialization.SerializationException;
import se.l4.aurochs.serialization.Serializer;
import se.l4.aurochs.serialization.format.StreamingInput;
import se.l4.aurochs.serialization.format.StreamingOutput;

import com.google.common.base.Throwables;

/**
 * Definition of a field within a reflection serializer.
 * 
 * @author Andreas Holstenson
 *
 */
public class FieldDefinition
{
	private final Field field;
	private final Serializer serializer;
	private final String name;
	private final Class<?> type;
	private final boolean readOnly;

	public FieldDefinition(Field field, String name, Serializer serializer, Class type)
	{
		this.field = field;
		this.name = name;
		this.serializer = serializer;
		this.type = type;
		readOnly = Modifier.isFinal(field.getModifiers());
	}
	
	public boolean isReadOnly()
	{
		return readOnly;
	}
	
	public Class<?> getType()
	{
		return type;
	}
	
	public Object read(StreamingInput in)
		throws IOException
	{
		return serializer.read(in);
	}
	
	public void read(Object target, StreamingInput in)
		throws IOException
	{
		set(target, read(in));
	}
	
	public void set(Object target, Object value)
		throws IOException
	{
		try
		{
			field.set(target, value);
		}
		catch(Exception e)
		{
			Throwables.propagateIfPossible(e);
			
			throw new SerializationException("Unable to read object; " + e.getMessage(), e);
		}
	}
	
	public Object getValue(Object target)
	{
		try
		{
			return field.get(target);
		}
		catch(IllegalArgumentException e)
		{
			throw new SerializationException("Unable to write object; " + e.getMessage(), e);
		}
		catch(IllegalAccessException e)
		{
			throw new SerializationException("Unable to write object; " + e.getMessage(), e);
		}
		
	}
	
	public void write(Object target, StreamingOutput stream)
		throws IOException
	{
		Object value = getValue(target);
		
		if(value == null)
		{
			stream.writeNull(name);
		}
		else
		{
			serializer.write(value, name, stream);
		}
	}
}