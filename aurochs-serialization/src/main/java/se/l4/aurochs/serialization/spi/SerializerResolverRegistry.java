package se.l4.aurochs.serialization.spi;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import se.l4.aurochs.serialization.SerializationException;
import se.l4.aurochs.serialization.Serializer;
import se.l4.aurochs.serialization.SerializerCollection;
import se.l4.aurochs.serialization.SerializerOrResolver;
import se.l4.aurochs.serialization.Use;
import se.l4.aurochs.serialization.collections.ArraySerializerResolver;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.primitives.Primitives;

/**
 * Finder of {@link SerializerResolver}s, used when implementing a
 * {@link SerializerCollection}.
 * 
 * @author Andreas Holstenson
 *
 */
public class SerializerResolverRegistry
{
	private static final SerializerResolver<?> ARRAY_RESOLVER = new ArraySerializerResolver();
	
	private final InstanceFactory instanceFactory;
	private final NamingCallback naming;
	
	private final Map<Class<?>, SerializerResolver<?>> boundTypeToResolver;
	private final LoadingCache<Class<?>, Optional<SerializerResolver<?>>> typeToResolverCache;

	public SerializerResolverRegistry(InstanceFactory instanceFactory, NamingCallback naming)
	{
		this.instanceFactory = instanceFactory;
		this.naming = naming;
		
		boundTypeToResolver = new ConcurrentHashMap<>();
		typeToResolverCache = CacheBuilder.newBuilder()
			.build(new CacheLoader<Class<?>, Optional<SerializerResolver<?>>>()
			{
				@Override
				public Optional<SerializerResolver<?>> load(Class<?> key)
					throws Exception
				{
					SerializerResolver<?> result = findOrCreateSerializerResolver(key);
					return Optional.<SerializerResolver<?>>fromNullable(result);
				}
			});
	}
	
	/**
	 * Bind a resolver for the given type.
	 * 
	 * @param type
	 * @param resolver
	 */
	public <T> void bind(Class<T> type, SerializerResolver<? extends T> resolver)
	{
		typeToResolverCache.put(type, Optional.<SerializerResolver<?>>of(resolver));
		boundTypeToResolver.put(type, resolver);
	}
	
	/**
	 * Get a resolver for the given type, returning {@code null} if
	 * the resolver can not be found.
	 * 
	 * @param type
	 *   the {@link Class} to find a resolver for
	 * @return
	 *   the found resolver, or {@code null} if no resolver is found
	 * @throws SerializationException
	 *   if the resolver could not be constructed from some reason
	 */
	public SerializerResolver<?> getResolver(Class<?> type)
	{
		try
		{
			Optional<SerializerResolver<?>> optional = typeToResolverCache.get(Primitives.wrap(type));
			
			return optional.orNull();
		}
		catch (ExecutionException e)
		{
			Throwables.propagateIfInstanceOf(e.getCause(), SerializationException.class);
			
			throw new SerializationException("Unable to retrieve serializer for " + type + "; " + e.getCause().getMessage(), e.getCause());
		}
	}
	
	protected SerializerResolver<?> findOrCreateSerializerResolver(Class<?> from)
	{
		SerializerResolver<?> resolver = createViaUse(from);
		if(resolver != null)
		{
			return resolver;
		}
		
		if(from.isArray())
		{
			// Arrays have special treatment, always use the array resolver
			return ARRAY_RESOLVER;
		}
		
		return findSerializerResolver(from);
	}
	
	protected SerializerResolver<?> findSerializerResolver(Class<?> type)
	{
		Class<?> parent = type;
		while(parent != null)
		{
			SerializerResolver<?> resolver = boundTypeToResolver.get(parent);
			if(resolver != null)
			{
				return resolver;
			}
			
			parent = parent.getSuperclass();
		}
		
		return null;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected SerializerResolver<?> createViaUse(Class<?> from)
	{
		if(from.isAnnotationPresent(Use.class))
		{
			// A specific serializer should be used
			Use annotation = from.getAnnotation(Use.class);
			final Class<? extends SerializerOrResolver> value = annotation.value();
			if(SerializerResolver.class.isAssignableFrom(value))
			{
				return (SerializerResolver<?>) instanceFactory.create(value);
			}
			
			Serializer serializer = instanceFactory.create((Class<? extends Serializer>) value);
			naming.registerIfNamed(from, serializer);
			return new StaticSerializerResolver(serializer);
		}
		
		return null;
	}
}
