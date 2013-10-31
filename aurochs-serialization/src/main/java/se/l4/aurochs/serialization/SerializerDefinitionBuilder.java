package se.l4.aurochs.serialization;

import java.lang.annotation.Annotation;

import se.l4.aurochs.serialization.format.ValueType;

/**
 * Builder of definitions, used from each {@link Serializer} to define
 * the type of fields in serialized form.
 * 
 * @author Andreas Holstenson
 *
 */
public interface SerializerDefinitionBuilder
{
	SerializerDefinitionBuilder object();
	
	/**
	 * Start adding a field to this definition. This implies that the
	 * type will be {@link #object()}.
	 *  
	 * @param name
	 * 		name of the field in its serialized form
	 * @return
	 * 		builder for the field
	 */
	FieldBuilder field(String name);

	/**
	 * Define that we represent a certain type of value.
	 * 
	 * @param valueType
	 * @return
	 */
	SerializerDefinitionBuilder value(ValueType valueType);
	
	/**
	 * Define that we represent a list.
	 * 
	 * @return
	 */
	SerializerDefinitionBuilder list();
	
	/**
	 * Build the definition.
	 * 
	 * @return
	 */
	SerializerDefinition build();
	
	/**
	 * Builder for field definition for object.
	 * 
	 * @author Andreas Holstenson
	 *
	 */
	interface FieldBuilder
	{
		/**
		 * Add a hint to the definition.
		 * 
		 * @param hint
		 * @return
		 */
		FieldBuilder withHint(Annotation hint);
		
		/**
		 * Add several hints to the definition.
		 * 
		 * @param hints
		 * @return
		 */
		FieldBuilder withHints(Annotation... hints);
		
		/**
		 * Define that this field uses the specified serializer.
		 * 
		 * @param serializer
		 * @return
		 */
		SerializerDefinitionBuilder using(Serializer<?> serializer);
		
		/**
		 * Define that this field uses the specified definition.
		 * 
		 * @param def
		 * @return
		 */
		SerializerDefinitionBuilder using(SerializerDefinition def);
	}
}
