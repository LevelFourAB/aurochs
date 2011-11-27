package se.l4.aurochs.serialization.collections;

import se.l4.aurochs.serialization.Serializer;
import se.l4.aurochs.serialization.spi.SerializerResolver;
import se.l4.aurochs.serialization.spi.TypeEncounter;
import se.l4.aurochs.serialization.spi.TypeViaClass;

/**
 * Resolver for array types.
 * 
 * @author Andreas Holstenson
 *
 */
public class ArraySerializerResolver
	implements SerializerResolver
{

	@Override
	public Serializer find(TypeEncounter encounter)
	{
		// TODO: Generics?
		
		Class<?> componentType = encounter.getType().getErasedType()
			.getComponentType();
		
		Serializer<?> itemSerializer = encounter.getCollection()
			.find(new TypeViaClass(componentType));
		
		return new ArraySerializer(componentType, itemSerializer);
	}

}
