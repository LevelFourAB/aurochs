package se.l4.aurochs.serialization;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import se.l4.aurochs.serialization.spi.InstanceFactory;

/**
 * Annotation for usage with constructors. This can be placed on constructors that
 * want to be injected via {@link InstanceFactory}.
 * 
 * @author Andreas Holstenson
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.CONSTRUCTOR })
@Documented
public @interface Factory
{
}
