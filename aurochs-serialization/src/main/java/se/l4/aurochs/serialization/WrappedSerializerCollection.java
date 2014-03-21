package se.l4.aurochs.serialization;

import se.l4.aurochs.serialization.spi.InstanceFactory;
import se.l4.aurochs.serialization.spi.NamingCallback;
import se.l4.aurochs.serialization.spi.SerializerResolver;
import se.l4.aurochs.serialization.spi.SerializerResolverRegistry;

/**
 * Implementation of {@link SerializerCollection} that wraps another
 * collection.
 * 
 * @author Andreas Holstenson
 *
 */
public class WrappedSerializerCollection
	extends AbstractSerializerCollection
{
	private final SerializerResolverRegistry resolverRegistry;
	private final SerializerCollection other;
	
	public WrappedSerializerCollection(SerializerCollection other)
	{
		this.other = other;
		resolverRegistry = new SerializerResolverRegistry(
			other.getInstanceFactory(),
			new NamingCallback()
			{
				@Override
				public void registerIfNamed(Class<?> from, Serializer<?> serializer)
				{
					WrappedSerializerCollection.this.registerIfNamed(from, serializer);
				}
			}
		);
	}
	
	@Override
	public InstanceFactory getInstanceFactory()
	{
		return other.getInstanceFactory();
	}
	
	@Override
	public <T> SerializerCollection bind(Class<T> type, SerializerResolver<? extends T> resolver)
	{
		resolverRegistry.bind(type, resolver);
		
		return this;
	}
	
	@Override
	public SerializerResolver<?> getResolver(Class<?> type)
	{
		SerializerResolver<?> resolver = other.getResolver(type);
		if(resolver != null)
		{
			return resolver;
		}
		
		return resolverRegistry.getResolver(type);
	}
}
