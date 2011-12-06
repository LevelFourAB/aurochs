package se.l4.aurochs.serialization;

import java.io.IOException;
import java.lang.reflect.Field;

import se.l4.aurochs.serialization.format.StreamingInput;
import se.l4.aurochs.serialization.format.StreamingInput.Token;
import se.l4.aurochs.serialization.format.StreamingOutput;
import se.l4.aurochs.serialization.internal.TypeViaResolvedType;
import se.l4.aurochs.serialization.internal.reflection.FieldDefinition;
import se.l4.aurochs.serialization.standard.DynamicSerializer;

import com.fasterxml.classmate.MemberResolver;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.ResolvedTypeWithMembers;
import com.fasterxml.classmate.TypeResolver;
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
 *
 * @param <T>
 */
public class ReflectionSerializer<T>
	implements Serializer<T>
{
	private final Class<T> type;
	private final ImmutableMap<String, FieldDefinition> fields;
	private final FieldDefinition[] fieldCache;

	public ReflectionSerializer(SerializerCollection collection, Class<T> type)
	{
		this.type = type;
		
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
			FieldDefinition def = new FieldDefinition(reflectiveField, name, serializer);
			builder.put(name, def);
		}
		
		fields = builder.build();
		fieldCache = fields.values().toArray(new FieldDefinition[0]);
	}
	
	private String getName(Field field)
	{
		if(field.isAnnotationPresent(Named.class))
		{
			Named named = field.getAnnotation(Named.class);
			if(! named.namespace().isEmpty())
			{
				throw new SerializationException(
					"Fields can not have namespaces in @" + 
					Named.class.getSimpleName() + 
					" (for " + field + ")"
				);
			}
			
			return named.name();
		}
		
		return field.getName();
	}
	
	private T createViaDefaultConstructor()
	{
		try
		{
			return type.newInstance();
		}
		catch(InstantiationException e)
		{
			throw new SerializationException("Unable to create; " + e.getMessage());
		}
		catch(IllegalAccessException e)
		{
			throw new SerializationException("Unable to create; " + e.getMessage());
		}
	}

	@Override
	public T read(StreamingInput in)
		throws IOException
	{
		in.next(Token.OBJECT_START);
		
		T instance = createViaDefaultConstructor();
		while(in.peek() != Token.OBJECT_END)
		{
			in.next(Token.KEY);
			String key = in.getString();
			
			FieldDefinition def = fields.get(key);
			if(def == null)
			{
				// No such field, skip the entire value
				in.skipValue();
			}
			else
			{
				def.read(instance, in);
			}
		}
		
		in.next(Token.OBJECT_END);
		return instance;
	}

	@Override
	public void write(T object, String name, StreamingOutput stream)
		throws IOException
	{
		stream.writeObjectStart(name);
		
		for(FieldDefinition def : fieldCache)
		{
			def.write(object, stream);
		}
		
		stream.writeObjectEnd(name);
	}
}
