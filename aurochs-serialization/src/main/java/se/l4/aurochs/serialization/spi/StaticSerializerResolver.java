package se.l4.aurochs.serialization.spi;

import se.l4.aurochs.serialization.Serializer;

/**
 * Resolver for types that have only one serializer.
 * 
 * @author Andreas Holstenson
 *
 * @param <T>
 */
public class StaticSerializerResolver<T>
	extends AbstractSerializerResolver<T>
{
	private final Serializer<T> serializer;

	public StaticSerializerResolver(Serializer<T> serializer)
	{
		this.serializer = serializer;
	}
	
	@Override
	public Serializer<T> find(TypeEncounter encounter)
	{
		return serializer;
	}
}