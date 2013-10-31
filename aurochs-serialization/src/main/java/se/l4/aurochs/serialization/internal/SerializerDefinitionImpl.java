package se.l4.aurochs.serialization.internal;

import java.lang.annotation.Annotation;
import java.util.Collection;

import se.l4.aurochs.serialization.SerializerDefinition;
import se.l4.aurochs.serialization.format.ValueType;

import com.google.common.collect.ImmutableMap;

public class SerializerDefinitionImpl
	implements SerializerDefinition
{
	private final int type;
	private final ValueType valueType;
	private final ImmutableMap<String, FieldDefinition> fields;

	public SerializerDefinitionImpl(int type, ValueType valueType, Iterable<FieldDefinition> definitions)
	{
		ImmutableMap.Builder<String, FieldDefinition> builder = ImmutableMap.builder();
		for(FieldDefinition fd : definitions)
		{
			builder.put(fd.getName(), fd);
		}
		
		this.fields = builder.build();
		
		this.type = type;
		this.valueType = valueType;
	}
	
	@Override
	public FieldDefinition getField(String fieldName)
	{
		return fields.get(fieldName);
	}
	
	@Override
	public Collection<FieldDefinition> getFields()
	{
		return fields.values();
	}
	
	@Override
	public ValueType getValueType()
	{
		return valueType;
	}
	
	@Override
	public boolean isList()
	{
		return type == 2;
	}
	
	@Override
	public boolean isObject()
	{
		return type == 1;
	}
	
	@Override
	public boolean isValue()
	{
		return type == 0;
	}
	
	public static class FieldDefintionImpl
		implements FieldDefinition
	{
		private final String name;
		private final SerializerDefinition definition;
		private final Annotation[] hints;

		public FieldDefintionImpl(String name, SerializerDefinition definition, Annotation[] hints)
		{
			this.name = name;
			this.definition = definition;
			this.hints = hints;
		}

		@Override
		public String getName()
		{
			return name;
		}

		@Override
		public SerializerDefinition getDefinition()
		{
			return definition;
		}

		@Override
		public Annotation[] getHints()
		{
			return hints;
		}
	
	}
}
