package se.l4.aurochs.serialization.spi;

/**
 * Type information with information about generics.
 * 
 * @author Andreas Holstenson
 *
 */
public interface Type
{
	/**
	 * Get the erased type.
	 * 
	 * @return
	 */
	Class<?> getErasedType();
	
	/**
	 * Get all of the parameters for this type.
	 * 
	 * @return
	 */
	Type[] getParameters();
}
