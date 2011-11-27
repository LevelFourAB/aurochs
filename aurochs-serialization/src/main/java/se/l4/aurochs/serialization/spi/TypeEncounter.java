package se.l4.aurochs.serialization.spi;

import java.lang.annotation.Annotation;

import se.l4.aurochs.serialization.SerializerCollection;

/**
 * Encounter with a specific type during serialization resoltuion.
 * 
 * @author Andreas Holstenson
 *
 */
public interface TypeEncounter
{
	/**
	 * Get the collection this encounter is for.
	 * 
	 * @return
	 */
	SerializerCollection getCollection();
	
	/**
	 * Get the type encountered.
	 * 
	 * @return
	 */
	Type getType();
	
	/**
	 * Fetch a hint of the specific type if available.
	 * 
	 * @param type
	 * @return
	 * 		the hint if found, or {@code null}
	 */
	<T extends Annotation> T getHint(Class<T> type);
}
