package se.l4.aurochs.serialization;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import se.l4.aurochs.serialization.format.StreamingInput;
import se.l4.aurochs.serialization.format.StreamingOutput;
import se.l4.aurochs.serialization.internal.TypeViaResolvedType;
import se.l4.aurochs.serialization.internal.reflection.FactoryDefinition;
import se.l4.aurochs.serialization.internal.reflection.FieldDefinition;
import se.l4.aurochs.serialization.internal.reflection.ReflectionNonStreamingSerializer;
import se.l4.aurochs.serialization.internal.reflection.ReflectionStreamingSerializer;
import se.l4.aurochs.serialization.internal.reflection.TypeInfo;
import se.l4.aurochs.serialization.standard.DynamicSerializer;

import com.fasterxml.classmate.MemberResolver;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.ResolvedTypeWithMembers;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.classmate.members.ResolvedConstructor;
import com.fasterxml.classmate.members.ResolvedField;
import com.google.common.collect.ImmutableMap;

/**
 * Serializer that will use reflection to access fields and methods in a
 * class. Will export anything annotated with {@link Expose}.
 * 
 * <p>
 * <ul>
 * 	<li>{@link Named} can be used if you want a field to have a specific name
 * 		in serialized form.
 * 	<li>If you need to use a custom serializer for a field annotate it with
 * 		{@link Use}.
 * 	<li>{@link AllowAny} will cause dynamic serialization to be used for a
 * 		field.
 * </ul>
 * 
 * @author Andreas Holstenson
 */
public class ReflectionSerializer<T>
	implements Serializer<T>
{
	private ReflectionSerializer()
	{
	}
	
	@Override
	public T read(StreamingInput in)
		throws IOException
	{
		return null;
	}
	
	public void write(T object, String name, StreamingOutput stream)
		throws IOException
	{
	}
	
	/**
	 * Create a new serializer that will use reflection.
	 * 
	 * @param type
	 * @param collection
	 * @return
	 */
	public static <T> Serializer<T> create(Class<T> type, SerializerCollection collection)
	{
		// TODO: Support for constructors
		
		ImmutableMap.Builder<String, FieldDefinition> builder = ImmutableMap.builder();
		
		TypeResolver typeResolver = new TypeResolver();
		MemberResolver memberResolver = new MemberResolver(typeResolver);
		ResolvedType rt = typeResolver.resolve(type);
		
		ResolvedTypeWithMembers typeWithMembers = memberResolver.resolve(rt, null, null);
		
		for(ResolvedField field : typeWithMembers.getMemberFields())
		{
			Field reflectiveField = field.getRawMember();
			
			if(! reflectiveField.isAnnotationPresent(Expose.class))
			{
				continue;
			}
			
			// Resolve the serializer to use for the field
			ResolvedType fieldType = field.getType();
			
			Serializer<?> serializer;
			if(reflectiveField.isAnnotationPresent(Use.class))
			{
				// Serializer has been set to a specific type
				Use annotation = reflectiveField.getAnnotation(Use.class);
				serializer = collection.getInstanceFactory().create(annotation.value());
			}
			else if(reflectiveField.isAnnotationPresent(AllowAny.class))
			{
				serializer = new DynamicSerializer(collection);
			}
			else
			{
				// Dynamically find a suitable type
				serializer = collection.find(new TypeViaResolvedType(fieldType), reflectiveField.getAnnotations());
			}

			// Force the field to be accessible
			reflectiveField.setAccessible(true);
			
			// Define how we access this field
			String name = getName(reflectiveField);
			FieldDefinition def = new FieldDefinition(reflectiveField, name, serializer, fieldType.getErasedType());
			builder.put(name, def);
		}
		
		// Create field map and cache
		ImmutableMap<String, FieldDefinition> fields = builder.build();
		FieldDefinition[] fieldsCache = fields.values().toArray(new FieldDefinition[0]);
		
		// Get all of the factories
		boolean hasSerializerInFactory = false;
		List<FactoryDefinition<T>> factories = new ArrayList<FactoryDefinition<T>>();
		
		for(ResolvedConstructor constructor : typeWithMembers.getConstructors())
		{
			FactoryDefinition<T> def = new FactoryDefinition<T>(collection, fields, constructor);
			hasSerializerInFactory |= def.hasSerializedFields();
			
			factories.add(def);
		}
		
		if(factories.isEmpty())
		{
			throw new SerializationException("Unable to create any instance of " + type + ", at least a default constructor is needed");
		}
		
		FactoryDefinition<T>[] factoryCache = factories.toArray(new FactoryDefinition[factories.size()]);
		
		// Create the actual serializer to use
		TypeInfo<T> typeInfo = new TypeInfo<T>(type, factoryCache, fields, fieldsCache);
		
		return hasSerializerInFactory 
			? new ReflectionNonStreamingSerializer<T>(typeInfo)
			: new ReflectionStreamingSerializer<T>(typeInfo);
	}
	
	private static String getName(Field field)
	{
		if(field.isAnnotationPresent(Expose.class))
		{
			Expose annotation = field.getAnnotation(Expose.class);
			if(! "".equals(annotation.value()))
			{
				return annotation.value();
			}
		}
		
		return field.getName();
	}
}
