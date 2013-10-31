package se.l4.aurochs.serialization;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import se.l4.aurochs.serialization.SerializerDefinition.FieldDefinition;
import se.l4.aurochs.serialization.format.ValueType;
import se.l4.aurochs.serialization.internal.SerializerDefinitionImpl;

/**
 * Default implementation of {@link SerializerDefinitionBuilder}.
 * 
 * @author Andreas Holstenson
 *
 */
public class DefaultSerializerDefinitionBuilder
	implements SerializerDefinitionBuilder
{
	private static final Annotation[] EMPTY_ANNOTATIONS = new Annotation[0];
	
	private final List<FieldDefinition> fields;
	private int type;
	private ValueType valueType;
	
	public DefaultSerializerDefinitionBuilder()
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
	public SerializerDefinitionBuilder list()
	{
		type = 2;
		return this;
	}
	
	@Override
	public SerializerDefinitionBuilder object()
	{
		type = 1;
		return this;
	}
	
	@Override
	public SerializerDefinitionBuilder value(ValueType valueType)
	{
		type = 0;
		this.valueType = valueType;
		return this;
	}
	
	@Override
	public SerializerDefinition build()
	{
		return new SerializerDefinitionImpl(type, valueType, fields);
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
		public SerializerDefinitionBuilder using(Serializer<?> serializer)
		{
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public SerializerDefinitionBuilder using(SerializerDefinition def)
		{
			fields.add(new SerializerDefinitionImpl.FieldDefintionImpl(
				name,
				def,
				hints.isEmpty() ? EMPTY_ANNOTATIONS : hints.toArray(EMPTY_ANNOTATIONS)
			));
			return DefaultSerializerDefinitionBuilder.this;
		}
	}
}
