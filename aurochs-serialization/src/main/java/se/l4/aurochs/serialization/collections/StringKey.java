package se.l4.aurochs.serialization.collections;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Map;

/**
 * Indicate that a {@link Map} has a string key and that key should be treated
 * as an object key during serialization.
 * 
 * @author Andreas Holstenson
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface StringKey
{

}
