package se.l4.aurochs.serialization;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that can be placed on a class that should be serialized to
 * control under which name it is exposed.
 * 
 * @author Andreas Holstenson
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD })
@Documented
public @interface Named
{
	/**
	 * Optional namespace of the class.
	 * 
	 * @return
	 */
	String namespace() default "";
	
	/**
	 * Name of the class.
	 * 
	 * @return
	 */
	String name();
}
