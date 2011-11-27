package se.l4.aurochs.serialization.internal;

import java.lang.annotation.Annotation;

import se.l4.aurochs.serialization.SerializerCollection;
import se.l4.aurochs.serialization.spi.Type;
import se.l4.aurochs.serialization.spi.TypeEncounter;

/**
 * Implementation of {@link TypeEncounter}.
 * 
 * @author Andreas Holstenson
 *
 */
public class TypeEncounterImpl
	implements TypeEncounter
{
	private final SerializerCollection collection;
	private final Type type;
	private final Annotation[] annotations;

	public TypeEncounterImpl(SerializerCollection collection, 
			Type type, 
			Annotation[] annotations)
	{
		this.collection = collection;
		this.type = type;
		this.annotations = annotations;
	}
	
	@Override
	public SerializerCollection getCollection()
	{
		return collection;
	}
	
	@Override
	public <T extends Annotation> T getHint(Class<T> type)
	{
		if(annotations == null) return null;
		
		for(Annotation a : annotations)
		{
			if(type.isAssignableFrom(a.getClass()))
			{
				return (T) a;
			}
		}
		
		return null;
	}
	
	@Override
	public Type getType()
	{
		return type;
	}
}
