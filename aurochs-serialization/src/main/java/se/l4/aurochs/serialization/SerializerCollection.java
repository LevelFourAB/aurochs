package se.l4.aurochs.serialization;

import java.lang.annotation.Annotation;

import se.l4.aurochs.serialization.spi.InstanceFactory;
import se.l4.aurochs.serialization.spi.SerializerResolver;
import se.l4.aurochs.serialization.spi.Type;


/**
 * Collection of {@link Serializer}s and {@link SerializerResolver resolvers}.
 * 
 * @author Andreas Holstenson
 *
 */
public interface SerializerCollection
{
	/**
	 * Get the current instance factory.
	 * 
	 * @return
	 */
	InstanceFactory getInstanceFactory();
	
	/**
	 * Bind a certain type automatically discovering which serializer to
	 * use.
	 * 
	 * @param type
	 */
	SerializerCollection bind(Class<?> type);
	
	/**
	 * Bind a given type to the specified serializer.
	 * 
	 * @param <T>
	 * @param type
	 * @param serializer
	 */
	<T> SerializerCollection bind(Class<T> type, Serializer<T> serializer);
	
	/**
	 * Bind a given type to the specified resolver. The resolver will be
	 * asked to resolve a more specific serializer based on type parameters.
	 * 
	 * @param <T>
	 * @param type
	 * @param resolver
	 */
	<T> SerializerCollection bind(Class<T> type, SerializerResolver<? extends T> resolver);
	
	/**
	 * Find a serializer suitable for the specific type.
	 * 
	 * @param <T>
	 * @param type
	 * @return
	 */
	<T> Serializer<T> find(Class<T> type);
	
	/**
	 * Find a serializer suitable for the specific type.
	 * 
	 * @param <T>
	 * @param type
	 * @return
	 */
	<T> Serializer<T> find(Class<T> type, Annotation... hints);
	
	/**
	 * Find a serializer suitable for the specified type.
	 * 
	 * @param type
	 * @return
	 */
	Serializer<?> find(Type type);
	
	/**
	 * Find a serializer suitable for the specified type.
	 * 
	 * @param type
	 * @return
	 */
	Serializer<?> find(Type type, Annotation... hints);
	
	/**
	 * Find a serializer based on its registered name.
	 * 
	 * @param name
	 * @return
	 */
	Serializer<?> find(String name);
	
	/**
	 * Find a serializer based on its registered name.
	 * 
	 * @param namespace
	 * @param name
	 * @return
	 */
	Serializer<?> find(String namespace, String name);
	
	/**
	 * Find the name of the given serializer (if any).
	 * 
	 * @param serializer
	 * @return
	 */
	QualifiedName findName(Serializer<?> serializer);
}
