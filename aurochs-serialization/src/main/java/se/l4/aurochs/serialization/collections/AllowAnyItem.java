package se.l4.aurochs.serialization.collections;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that indicates that a collect can contain any type. This
 * will cause the serialization library to use dynamic resolution based on
 * names instead of relying on the reflected item type.
 * 
 * @author Andreas Holstenson
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD} )
@Documented
public @interface AllowAnyItem
{

}
