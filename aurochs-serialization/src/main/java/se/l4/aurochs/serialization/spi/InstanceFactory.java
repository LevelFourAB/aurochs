package se.l4.aurochs.serialization.spi;

import java.lang.annotation.Annotation;

/**
 * Factory for instances, used to support dependency injection if available.
 * 
 * @author Andreas Holstenson
 *
 */
public interface InstanceFactory
{
	/**
	 * Create the specified type.
	 * 
	 * @param <T>
	 * @param type
	 * @return
	 */
	<T> T create(Class<T> type);
	
	/**
	 * Create the specified type using the given annotation as hints.
	 * 
	 * @param type
	 * @param annotations
	 * @return
	 */
	<T> T create(Class<T> type, Annotation[] annotations);
}
