package se.l4.aurochs.core.id;

/**
 * Generator for long based identifiers.
 * 
 * @author Andreas Holstenson
 *
 */
public interface LongIdGenerator
{
	/**
	 * Get the next identifier.
	 * 
	 * @return
	 */
	long next();
}
