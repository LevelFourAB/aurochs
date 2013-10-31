package se.l4.aurochs.serialization;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

import se.l4.aurochs.serialization.format.ValueType;

/**
 * Definition describing the serialized format that is read or
 * written by a {@link Serializer}.
 * 
 * @author Andreas Holstenson
 *
 */
public interface SerializerDefinition
{
	/**
	 * Get if the described format is a value.
	 * 
	 * @return
	 */
	boolean isValue();
	
	/**
	 * Get the type of value.
	 * 
	 * @return
	 */
	ValueType getValueType();
	
	/**
	 * Get if the described format is a list.
	 * 
	 * @return
	 */
	boolean isList();
	
	/**
	 * Get if the described format is a object.
	 * 
	 * @return
	 */
	boolean isObject();
	
	/**
	 * Get a field from this definition, only works if this is a description
	 * of an object.
	 * 
	 * @param fieldName
	 * @return
	 */
	FieldDefinition getField(String fieldName);
	
	/**
	 * Get all fields.
	 * 
	 * @return
	 */
	Collection<FieldDefinition> getFields();
	
	/**
	 * Definition of a field in an object.
	 * 
	 * @author Andreas Holstenson
	 *
	 */
	interface FieldDefinition
	{
		/**
		 * Get the name of the field.
		 * 
		 * @return
		 */
		String getName();
		
		/**
		 * Get the definition of this field.
		 * 
		 * @return
		 */
		SerializerDefinition getDefinition();
		
		/**
		 * Get any hints found on this field.
		 * 
		 * @return
		 */
		Annotation[] getHints();
	}
}
