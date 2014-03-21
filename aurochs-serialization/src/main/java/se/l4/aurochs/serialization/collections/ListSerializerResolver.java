package se.l4.aurochs.serialization.collections;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

import se.l4.aurochs.serialization.SerializationException;
import se.l4.aurochs.serialization.Serializer;
import se.l4.aurochs.serialization.spi.AbstractSerializerResolver;
import se.l4.aurochs.serialization.spi.Type;
import se.l4.aurochs.serialization.spi.TypeEncounter;
import se.l4.aurochs.serialization.spi.TypeViaClass;
import se.l4.aurochs.serialization.standard.DynamicSerializer;

import com.google.common.collect.ImmutableSet;

public class ListSerializerResolver
	extends AbstractSerializerResolver<List<?>>
{
	private static final Set<Class<? extends Annotation>> HINTS =
		ImmutableSet.<Class<? extends Annotation>>of(AllowAnyItem.class);
	
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Serializer<List<?>> find(TypeEncounter encounter)
	{
		Type[] params = encounter.getType().getParameters();
		Type type = params.length == 0 ? new TypeViaClass(Object.class) : params[0];  
		
		// Check that we can create the type of list requested
		Class<?> erasedType = encounter.getType().getErasedType();
		if(erasedType != List.class)
		{
			throw new SerializationException("Lists can only be serialized if they are declared as the interface List");
		}
		
		// Create the serializer, either a specific or dynamic
		Serializer<?> itemSerializer =
			encounter.getHint(AllowAnyItem.class) == null
				? encounter.getCollection().find(type)
				: new DynamicSerializer(encounter.getCollection());
			
		return new ListSerializer(itemSerializer);
	}

	@Override
	public Set<Class<? extends Annotation>> getHints()
	{
		return HINTS;
	}
}
