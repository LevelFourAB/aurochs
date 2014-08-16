package se.l4.aurochs.serialization;

import java.lang.reflect.Field;
import java.util.List;

import se.l4.aurochs.serialization.internal.TypeViaResolvedType;
import se.l4.aurochs.serialization.internal.reflection.FactoryDefinition;
import se.l4.aurochs.serialization.internal.reflection.FieldDefinition;
import se.l4.aurochs.serialization.internal.reflection.ReflectionNonStreamingSerializer;
import se.l4.aurochs.serialization.internal.reflection.ReflectionStreamingSerializer;
import se.l4.aurochs.serialization.internal.reflection.TypeInfo;
import se.l4.aurochs.serialization.spi.AbstractSerializerResolver;
import se.l4.aurochs.serialization.spi.SerializerResolver;
import se.l4.aurochs.serialization.spi.Type;
import se.l4.aurochs.serialization.spi.TypeEncounter;
import se.l4.aurochs.serialization.standard.CompactDynamicSerializer;
import se.l4.aurochs.serialization.standard.DynamicSerializer;

import com.fasterxml.classmate.MemberResolver;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.ResolvedTypeWithMembers;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.classmate.members.ResolvedConstructor;
import com.fasterxml.classmate.members.ResolvedField;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

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
	extends AbstractSerializerResolver<T>
{
	public ReflectionSerializer()
	{
	}
	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Serializer<T> find(TypeEncounter encounter)
	{
		Type type = encounter.getType();
		SerializerCollection collection = encounter.getCollection();
		
		ImmutableMap.Builder<String, FieldDefinition> builder = ImmutableMap.builder();
		ImmutableMap.Builder<String, FieldDefinition> nonRenamedFields = ImmutableMap.builder();
		
		TypeResolver typeResolver = new TypeResolver();
		MemberResolver memberResolver = new MemberResolver(typeResolver);
		
		Type[] params = type.getParameters();
		ResolvedType rt = params.length == 0 
			? typeResolver.resolve(type.getErasedType())
			: resolveWithParams(typeResolver, type);
		
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
				if(Serializer.class.isAssignableFrom(annotation.value()))
				{
					try
					{
						serializer = (Serializer) collection.getInstanceFactory().create(annotation.value());
					}
					catch(Exception e)
					{
						throw new SerializationException("Unable to create " + annotation.value() + " for " + type + "; " + e.getMessage(), e);
					}
				}
				else if(SerializerResolver.class.isAssignableFrom(annotation.value()))
				{
					serializer = collection.findVia((Class) annotation.value(), new TypeViaResolvedType(fieldType), reflectiveField.getAnnotations());
				}
				else
				{
					throw new SerializationException("Unable to create " + annotation.value() + " for " +  type + "; Class either needs to a Serializer or a SerializerResolver");
				}
			}
			else if(reflectiveField.isAnnotationPresent(AllowAny.class))
			{
				AllowAny allowAny = reflectiveField.getAnnotation(AllowAny.class);
				serializer = allowAny.compact()
					? new CompactDynamicSerializer(collection)
					: new DynamicSerializer(collection);
			}
			else
			{
				// Dynamically find a suitable type
				try
				{
					serializer = collection.find(new TypeViaResolvedType(fieldType), reflectiveField.getAnnotations());
				}
				catch(SerializationException e)
				{
					throw new SerializationException("Could not resolve " + field.getName() + " for " + type.getErasedType() + "; " + e.getMessage(), e);
				}
			}
			
			if(serializer == null)
			{
				throw new SerializationException("Could not resolve " + field.getName() + " for " + type.getErasedType() + "; No serializer found");
			}

			// Force the field to be accessible
			reflectiveField.setAccessible(true);
			
			// Define how we access this field
			String name = getName(reflectiveField);
			FieldDefinition def = new FieldDefinition(reflectiveField, name, serializer, fieldType.getErasedType());
			builder.put(name, def);
			nonRenamedFields.put(reflectiveField.getName(), def);
		}
		
		// Create field map and cache
		ImmutableMap<String, FieldDefinition> fields = builder.build();
		ImmutableMap<String, FieldDefinition> nonRenamed = builder.build();
		FieldDefinition[] fieldsCache = fields.values().toArray(new FieldDefinition[0]);
		
		// Get all of the factories
		boolean hasSerializerInFactory = false;
		List<FactoryDefinition<T>> factories = Lists.newArrayList();
		
		for(ResolvedConstructor constructor : typeWithMembers.getConstructors())
		{
			FactoryDefinition<T> def = new FactoryDefinition<T>(collection, fields, nonRenamed, constructor);
			hasSerializerInFactory |= def.hasSerializedFields();
			
			if(def.hasSerializedFields() || def.isInjectable())
			{
				factories.add(def);
			}
		}
		
		if(factories.isEmpty())
		{
			throw new SerializationException("Unable to create any instance of " + type + ", at least a default constructor is needed");
		}
		
		FactoryDefinition<T>[] factoryCache = factories.toArray(new FactoryDefinition[factories.size()]);
		
		// Create the actual serializer to use
		TypeInfo<T> typeInfo = new TypeInfo<T>((Class) type.getErasedType(), factoryCache, fields, fieldsCache);
		
		return hasSerializerInFactory 
			? new ReflectionNonStreamingSerializer<T>(typeInfo)
			: new ReflectionStreamingSerializer<T>(typeInfo);
	}
	
	private static ResolvedType resolveWithParams(TypeResolver typeResolver, Type type)
	{
		if(type instanceof TypeViaResolvedType)
		{
			return ((TypeViaResolvedType) type).getResolvedType();
		}
		
		return null;
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
