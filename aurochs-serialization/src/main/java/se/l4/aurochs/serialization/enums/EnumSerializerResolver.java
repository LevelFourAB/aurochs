package se.l4.aurochs.serialization.enums;

import java.lang.reflect.Constructor;

import se.l4.aurochs.serialization.SerializationException;
import se.l4.aurochs.serialization.Serializer;
import se.l4.aurochs.serialization.spi.SerializerResolver;
import se.l4.aurochs.serialization.spi.TypeEncounter;

import com.google.common.base.Throwables;

/**
 * Resolver for {@link Enum enums}, can handle any enum type and supports
 * different translators between serialized and object form.
 * 
 * @author Andreas Holstenson
 *
 */
public class EnumSerializerResolver
	implements SerializerResolver<Enum<?>>
{

	@Override
	public Serializer<Enum<?>> find(TypeEncounter encounter)
	{
		Class<? extends Enum<?>> type = (Class<? extends Enum<?>>) encounter.getType().getErasedType();
		
		MapEnumVia hint = encounter.getHint(MapEnumVia.class);
		ValueTranslator translator;
		if(hint != null)
		{
			translator = create(hint.value(), type);
		}
		else
		{
			translator = new NameTranslator(type);
		}
		
		return new EnumSerializer(translator);
	}

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
}
