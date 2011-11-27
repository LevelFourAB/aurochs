package se.l4.aurochs.serialization.spi;

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
}
