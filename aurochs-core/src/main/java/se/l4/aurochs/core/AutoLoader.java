package se.l4.aurochs.core;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * Plugin support.
 * 
 * @author Andreas Holstenson
 *
 */
public interface AutoLoader
{
	/**
	 * Get all classes of a certain type annotated with {@link AutoLoad}.
	 * 
	 * @param type
	 * @return
	 * @deprecated
	 *   use {@link #getAutoLoadedClassesOfType(Class)} instead
	 */
	@Deprecated
	<T> Set<Class<? extends T>> getPluginsOfType(Class<T> type);
	
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
	
	/**
	 * Get classes that have been annotated with a certain annotation.
	 * 
	 * @param annotationType
	 * @return
	 */
	Set<Class<?>> getClassesAnnotatedWith(Class<? extends Annotation> annotationType);

	/**
	 * Get sub types of the given class.
	 * 
	 * @param type
	 * @return
	 */
	<T> Set<Class<? extends T>> getSubTypesOf(Class<T> type);
	
	/**
	 * Get sub types of the given class automatically creating them via injection.
	 * 
	 * @param type
	 * @return
	 */
	<T> Set<? extends T> getSubTypesAsInstances(Class<T> type);
}
;