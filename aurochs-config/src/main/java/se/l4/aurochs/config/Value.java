package se.l4.aurochs.config;

/**
 * Value within a configuration.
 * 
 * @author Andreas Holstenson
 *
 * @param <T>
 */
public interface Value<T>
{
	/**
	 * Get the current value.
	 * 
	 * @return
	 */
	T get();
	
	/**
	 * Get the current value or return a default value if it is not set.
	 * 
	 * @param defaultInstance
	 * @return
	 */
	T getOrDefault(T defaultInstance);
	
	/**
	 * Get if this value exists.
	 * 
	 * @return
	 */
	boolean exists();
}
