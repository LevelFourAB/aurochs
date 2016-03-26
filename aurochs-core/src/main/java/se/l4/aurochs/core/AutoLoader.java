package se.l4.aurochs.core;

import java.util.Set;

import se.l4.commons.types.TypeFinder;

/**
 * Plugin support.
 * 
 * @author Andreas Holstenson
 *
 */
public interface AutoLoader
	extends TypeFinder
{
	/**
	 * Get all classes of a certain type annotated with {@link AutoLoad}.
	 * 
	 * @param type
	 * @return
	 */
	<T> Set<Class<? extends T>> getAutoLoadedClassesOfType(Class<T> type);
	
	/**
	 * Get instances of a certain type that are annotated with {@link AutoLoad}.
	 * 
	 * @param type
	 * @return
	 */
	<T> Set<? extends T> getAutoLoadedInstancesOfType(Class<T> type);
}
