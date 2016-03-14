package se.l4.aurochs.serialization;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicate that a field may contain any type that is compatible with the
 * declaration. This will cause the library to use dynamic serialization based
 * on names for the field.
 * 
 * @author Andreas Holstenson
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@Documented
public @interface AllowSimpleTypes
{
	/**
	 * Set if this should use the compact format or not. The compact format
	 * will write the container as a list, while the non-compact format is
	 * an object with the keys {@code namespace}, {@code name}, {@code value}.
	 * 
	 * @return
	 */
	boolean compact() default false;
}
