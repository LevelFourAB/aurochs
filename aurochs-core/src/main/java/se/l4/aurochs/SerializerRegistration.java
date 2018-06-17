package se.l4.aurochs;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.google.inject.BindingAnnotation;

/**
 * Annotation that can be used on a method in a module to run as part of
 * the serializer contributions.
 */
@BindingAnnotation
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SerializerRegistration
{
}
