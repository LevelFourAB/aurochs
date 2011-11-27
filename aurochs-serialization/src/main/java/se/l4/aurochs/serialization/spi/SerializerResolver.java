package se.l4.aurochs.serialization.spi;

import se.l4.aurochs.serialization.Serializer;

/**
 * Resolver for a specific {@link Serializer}. This is used to support
 * generics and other semi-dynamic features.
 * 
 * @author Andreas Holstenson
 *
 * @param <T>
 */
public interface SerializerResolver<T>
{
	/**
	 * Attempt to find a suitable serializer. Returning {@code null} will
	 * usually raise an error.
	 * 
	 * @param encounter
	 * @return
	 */
	Serializer<T> find(TypeEncounter encounter);
}
