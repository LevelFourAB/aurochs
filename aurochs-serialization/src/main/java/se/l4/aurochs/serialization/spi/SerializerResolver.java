package se.l4.aurochs.serialization.spi;

import java.lang.annotation.Annotation;
import java.util.Set;

import se.l4.aurochs.serialization.Serializer;
import se.l4.aurochs.serialization.SerializerOrResolver;

/**
 * Resolver for a specific {@link Serializer}. This is used to support
 * generics and other semi-dynamic features.
 *
 * <p>
 * Resolvers that use extra annotations to determine the serializer to use
 * should override {@link #getHints()} to return an array of the annotations
 * it uses.
 * 
 * @author Andreas Holstenson
 *
 * @param <T>
 */
public interface SerializerResolver<T>
	extends SerializerOrResolver<T>
{
	/**
	 * Attempt to find a suitable serializer.
	 * 
	 * @param encounter
	 * @return
	 */
	Serializer<T> find(TypeEncounter encounter);
	
	/**
	 * Get the hints this resolver uses.
	 * 
	 * @return
	 */
	Set<Class<? extends Annotation>> getHints();
}
