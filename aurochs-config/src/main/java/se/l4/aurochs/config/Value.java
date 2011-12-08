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
}
