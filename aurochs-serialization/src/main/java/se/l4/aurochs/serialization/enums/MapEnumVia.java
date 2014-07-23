package se.l4.aurochs.serialization.enums;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Annotation to give the serialization library a hint on how a certain enum 
 * should be mapped.
 *  
 * @author Andreas Holstenson
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.TYPE })
@Documented
public @interface MapEnumVia
{
	/**
	 * The translator to use. The default translator is 
	 * {@link NameTranslator}. {@link OrdinalTranslator} can be used to 
	 * serialize enums using {@link Enum#ordinal()}. 
	 * 
	 * @return
	 */
	Class<? extends ValueTranslator<?>> value();
}
