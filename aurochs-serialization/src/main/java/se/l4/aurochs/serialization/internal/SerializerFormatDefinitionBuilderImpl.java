package se.l4.aurochs.serialization.internal;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import se.l4.aurochs.serialization.Serializer;
import se.l4.aurochs.serialization.SerializerFormatDefinition;
import se.l4.aurochs.serialization.SerializerFormatDefinition.FieldDefinition;
import se.l4.aurochs.serialization.SerializerFormatDefinition.Builder;
import se.l4.aurochs.serialization.SerializerFormatDefinition.FieldBuilder;
import se.l4.aurochs.serialization.format.ValueType;

/**
 * Implementation of {@link SerializerDefinition.Builder}.
 * 
 * @author Andreas Holstenson
 *
 */
public class SerializerFormatDefinitionBuilderImpl
	implements Builder
{
	private static final Annotation[] EMPTY_ANNOTATIONS = new Annotation[0];
	
	private final List<FieldDefinition> fields;
	private int type;
	private ValueType valueType;
	private SerializerFormatDefinition itemDefinition;
	
	public SerializerFormatDefinitionBuilderImpl()
	{
		fields = new ArrayList<FieldDefinition>();
		type = 1;
	}

	@Override
	public FieldBuilder field(String name)
	{
		return new FieldBuilderImpl(name);
	}
	
	@Override
	public Builder list(SerializerFormatDefinition itemDefinition)
	{
		type = 2;
		this.itemDefinition = itemDefinition;
		return this;
	}
	
	@Override
	public Builder list(Serializer<?> itemSerializer)
	{
		return list(itemSerializer.getFormatDefinition());
	}
	
	@Override
	public Builder object()
	{
		type = 1;
		return this;
	}
	
	@Override
	public Builder value(ValueType valueType)
	{
		type = 0;
		this.valueType = valueType;
		return this;
	}
	
	@Override
	public SerializerFormatDefinition build()
	{
		if(type == 0)
		{
			return SerializerFormatDefinition.forValue(valueType);
		}
		
		return new SerializerFormatDefinition(type, valueType, fields);
	}
	
	private class FieldBuilderImpl
		implements FieldBuilder
	{
		private final Collection<Annotation> hints;
		private String name;
		
		public FieldBuilderImpl(String name)
		{
			this.name = name;
			hints = new LinkedHashSet<Annotation>();
		}

		@Override
		public FieldBuilder withHint(Annotation hint)
		{
			hints.add(hint);
			return this;
		}

		@Override
		public FieldBuilder withHints(Annotation... hints)
		{
			for(Annotation a : hints)
			{
				this.hints.add(a);
			}
			return this;
		}

		@Override
		public Builder using(Serializer<?> serializer)
		{
			return using(serializer.getFormatDefinition());
		}
		
		@Override
		public Builder using(ValueType valueType)
		{
			return using(SerializerFormatDefinition.forValue(valueType));
		}
		
		@Override
		public Builder using(SerializerFormatDefinition def)
		{
			fields.add(new SerializerFormatDefinition.FieldDefinition(
				name,
				def,
				hints.isEmpty() ? EMPTY_ANNOTATIONS : hints.toArray(EMPTY_ANNOTATIONS)
			));
			return SerializerFormatDefinitionBuilderImpl.this;
		}
	}
}
