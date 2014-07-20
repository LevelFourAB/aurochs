package se.l4.aurochs.serialization.enums;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Set;

import se.l4.aurochs.serialization.SerializationException;
import se.l4.aurochs.serialization.Serializer;
import se.l4.aurochs.serialization.spi.AbstractSerializerResolver;
import se.l4.aurochs.serialization.spi.TypeEncounter;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;

/**
 * Resolver for {@link Enum enums}, can handle any enum type and supports
 * different translators between serialized and object form.
 * 
 * @author Andreas Holstenson
 *
 */
public class EnumSerializerResolver
	extends AbstractSerializerResolver<Enum<?>>
{
	private static final Set<Class<? extends Annotation>> HINTS =
		ImmutableSet.<Class<? extends Annotation>>of(MapEnumVia.class);
	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Serializer<Enum<?>> find(TypeEncounter encounter)
	{
		Class<? extends Enum<?>> type = (Class<? extends Enum<?>>) encounter.getType().getErasedType();
		
		MapEnumVia hint = encounter.getHint(MapEnumVia.class);
		ValueTranslator translator;
		if(hint != null)
		{
			translator = create(hint.value(), type);
		}
		else if((hint = type.getAnnotation(MapEnumVia.class)) != null)
		{
			translator = create(hint.value(), type);
		}
		else
		{
			translator = new NameTranslator(type);
		}
		
		return new EnumSerializer(translator);
	}

	@SuppressWarnings("rawtypes")
	private ValueTranslator create(
			Class<? extends ValueTranslator> translator,
			Class<? extends Enum<?>> type)
	{
		for(Constructor c : translator.getConstructors())
		{
			Class[] types = c.getParameterTypes();
			if(types.length != 1) continue;
			
			Class<?> t = types[0];
			if(t.isAssignableFrom(Class.class))
			{
				try
				{
					return (ValueTranslator) c.newInstance(type);
				}
				catch(InstantiationException e)
				{
					Throwables.propagateIfInstanceOf(e.getCause(), SerializationException.class);
					throw new SerializationException("Unable to create; " + e.getCause().getMessage(), e.getCause());
				}
				catch(Exception e)
				{
					throw new SerializationException("Unable to create; " + e.getMessage(), e);
				}
			}
		}
		
		throw new SerializationException("Constructor that takes Enum is required (for " + translator + ")");
	}
	
	@Override
	public Set<Class<? extends Annotation>> getHints()
	{
		return HINTS;
	}
}
