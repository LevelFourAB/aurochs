package se.l4.aurochs.serialization;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import se.l4.aurochs.serialization.collections.ArraySerializerResolver;
import se.l4.aurochs.serialization.collections.ListSerializerResolver;
import se.l4.aurochs.serialization.collections.MapSerializerResolver;
import se.l4.aurochs.serialization.collections.SetSerializerResolver;
import se.l4.aurochs.serialization.enums.EnumSerializerResolver;
import se.l4.aurochs.serialization.internal.DelayedSerializer;
import se.l4.aurochs.serialization.internal.TypeEncounterImpl;
import se.l4.aurochs.serialization.spi.DefaultInstanceFactory;
import se.l4.aurochs.serialization.spi.InstanceFactory;
import se.l4.aurochs.serialization.spi.SerializerResolver;
import se.l4.aurochs.serialization.spi.Type;
import se.l4.aurochs.serialization.spi.TypeEncounter;
import se.l4.aurochs.serialization.spi.TypeViaClass;
import se.l4.aurochs.serialization.standard.BooleanSerializer;
import se.l4.aurochs.serialization.standard.ByteArraySerializer;
import se.l4.aurochs.serialization.standard.DoubleSerializer;
import se.l4.aurochs.serialization.standard.FloatSerializer;
import se.l4.aurochs.serialization.standard.IntSerializer;
import se.l4.aurochs.serialization.standard.LongSerializer;
import se.l4.aurochs.serialization.standard.ShortSerializer;
import se.l4.aurochs.serialization.standard.StringSerializer;
import se.l4.aurochs.serialization.standard.UuidSerializer;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.primitives.Primitives;
import com.google.common.util.concurrent.UncheckedExecutionException;

/**
 * Default implementation of {@link SerializerCollection}.
 * 
 * @author Andreas Holstenson
 *
 */
public class DefaultSerializerCollection
	implements SerializerCollection
{
	private static final SerializerResolver<?> ARRAY_RESOLVER = new ArraySerializerResolver();
	private static final ThreadLocal<Set<Class>> stack = new ThreadLocal<Set<Class>>();
	
	private final InstanceFactory instanceFactory;
	private final Map<Class<?>, SerializerResolver<?>> boundTypeToResolver;
	private final LoadingCache<Class<?>, Optional<SerializerResolver<?>>> typeToResolverCache;
	private final Map<QualifiedName, Serializer<?>> nameToSerializer;
	private final Map<Serializer<?>, QualifiedName> serializerToName;
	
	public DefaultSerializerCollection()
	{
		this(new DefaultInstanceFactory());
	}
	
	public DefaultSerializerCollection(InstanceFactory instanceFactory)
	{
		this.instanceFactory = instanceFactory;
		
		nameToSerializer = new ConcurrentHashMap<QualifiedName, Serializer<?>>();
		serializerToName = new ConcurrentHashMap<Serializer<?>, QualifiedName>();
		
		boundTypeToResolver = new ConcurrentHashMap<Class<?>, SerializerResolver<?>>();
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
		
		// Standard types
		bind(Boolean.class, new BooleanSerializer(), "", "boolean");
		bind(Float.class, new FloatSerializer(), "", "float");
		bind(Double.class, new DoubleSerializer(), "", "double");
		bind(Short.class, new ShortSerializer(), "", "short");
		bind(Integer.class, new IntSerializer(), "", "integer");
		bind(Long.class, new LongSerializer(), "", "long");
		bind(String.class, new StringSerializer(), "", "string");
		bind(byte[].class, new ByteArraySerializer(), "", "byte[]");
		bind(UUID.class, new UuidSerializer(), "", "uuid");
		
		// Collections
		bind(List.class, new ListSerializerResolver());
		bind(Map.class, new MapSerializerResolver());
		bind(Set.class, new SetSerializerResolver());
		
		// Enums
		bind(Enum.class, new EnumSerializerResolver());
	}
	
	@Override
	public InstanceFactory getInstanceFactory()
	{
		return instanceFactory;
	}
	
	private <T> void bind(Class<T> type, Serializer<T> serializer, String ns, String name)
	{
		bind(type, new StaticSerializer<T>(serializer));
		
		QualifiedName qname = new QualifiedName(ns, name);
		nameToSerializer.put(qname, serializer);
		serializerToName.put(serializer, qname);
	}

	@Override
	public SerializerCollection bind(Class<?> type)
	{
		find(type);
		
		return this;
	}
	
	@Override
	public <T> SerializerCollection bind(Class<T> type, Serializer<T> serializer)
	{
		bind(type, new StaticSerializer<T>(serializer));
		
		registerIfNamed(type, serializer);
		
		return this;
	}
	
	@Override
	public <T> SerializerCollection bind(Class<T> type, SerializerResolver<? extends T> resolver)
	{
		typeToResolverCache.put(type, Optional.<SerializerResolver<?>>of(resolver));
		boundTypeToResolver.put(type, resolver);
		
		return this;
	}
	
	@Override
	public <T> Serializer<T> find(Class<T> type)
	{
		return (Serializer<T>) find(new TypeViaClass(type));
	}
	
	@Override
	public <T> Serializer<T> find(Class<T> type, Annotation... hints)
	{
		return (Serializer<T>) find(new TypeViaClass(type), hints);
	}

	@Override
	public Serializer<?> find(Type type)
	{
		return find(type, (Annotation[]) null);
	}
	
	@Override
	public Serializer<?> find(Type type, Annotation... hints)
	{
		try
		{
			Set<Class> s = stack.get();
			if(s != null && s.contains(type.getErasedType()))
			{
				// Already trying to create this serializer, delay creation
				return new DelayedSerializer(this, type, hints);
			}
			
			SerializerResolver finder = getResolver(type.getErasedType(), true);
			
			Serializer serializer = finder.find(new TypeEncounterImpl(this, type, hints));
			if(serializer == null)
			{
				throw new SerializationException("Unable to find serializer for " + type);
			}
			
			return serializer;
		}
		catch(UncheckedExecutionException e)
		{
			Throwables.propagateIfInstanceOf(e.getCause(), SerializationException.class);
			
			throw new SerializationException("Unable to retrieve serializer for " + type + "; " + e.getCause().getMessage(), e.getCause());
		}
	}
	
	@Override
	public Serializer<?> find(String name)
	{
		return find("", name);
	}
	
	@Override
	public Serializer<?> find(String namespace, String name)
	{
		return nameToSerializer.get(new QualifiedName(namespace, name));
	}
	
	@Override
	public QualifiedName findName(Serializer<?> serializer)
	{
		return serializerToName.get(serializer);
	}
	
	@Override
	public boolean isSupported(Class<?> type)
	{
		SerializerResolver finder = getResolver(type, false);
		if(finder == null) return false;
		
		if(finder instanceof StaticSerializer)
		{
			return true;
		}
		
		Serializer serializer = finder.find(new TypeEncounterImpl(this, new TypeViaClass(type), null));
		return serializer != null;
	}
	
	protected SerializerResolver<?> getResolver(Class<?> type, boolean throwIfUnavailable)
	{
		try
		{
			Optional<SerializerResolver<?>> optional = typeToResolverCache.get(Primitives.wrap(type));
			
			if(throwIfUnavailable && ! optional.isPresent())
			{
				throw new SerializationException("Unable to retreive serializer for " + type + "; Type does not appear serializable");
			}
			
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
	
	protected SerializerResolver<?> createViaUse(Class<?> from)
	{
		if(from.isAnnotationPresent(Use.class))
		{
			// A specific serializer should be used
			Use annotation = from.getAnnotation(Use.class);
			final Class<? extends Serializer> value = annotation.value();
			Serializer serializer;
			if(value == ReflectionSerializer.class)
			{
				if(from.getTypeParameters().length == 0)
				{
					Set<Class> s = stack.get();
					if(s == null)
					{
						s = new HashSet<Class>();
						stack.set(s);
					}
					
					boolean remove = false;
					try
					{
						remove = s.add(from);
						serializer = ReflectionSerializer.create(new TypeViaClass(from), this);
					}
					finally
					{
						if(remove)
						{
							s.remove(from);
						}
						
						if(s.isEmpty())
						{
							stack.remove();
						}
					}
				}
				else
				{
					// Generic types are present
					return new SerializerResolver()
					{
						@Override
						public Serializer find(TypeEncounter encounter)
						{
							Set<Class> s = stack.get();
							if(s == null)
							{
								s = new HashSet<Class>();
								stack.set(s);
							}
							
							boolean remove = false;
							try
							{
								remove = s.add(encounter.getType().getErasedType());
								return ReflectionSerializer.create(encounter.getType(), DefaultSerializerCollection.this);
							}
							finally
							{
								if(remove)
								{
									s.remove(encounter.getType().getErasedType());
								}
								
								if(s.isEmpty())
								{
									stack.remove();
								}
							}
						}
					};
				}
			}
			else
			{
				serializer = instanceFactory.create(value);
			}
			
			registerIfNamed(from, serializer);
			return new StaticSerializer(serializer);
		}
		
		return null;
	}

	private void registerIfNamed(Class<?> from, Serializer<?> serializer)
	{
		if(from.isAnnotationPresent(Named.class))
		{
			Named named = from.getAnnotation(Named.class);
			QualifiedName key = new QualifiedName(named.namespace(), named.name());
			nameToSerializer.put(key, serializer);
			serializerToName.put(serializer, key);
		}
	}

	/**
	 * Resolver for types that have only one serializer.
	 * 
	 * @author Andreas Holstenson
	 *
	 * @param <T>
	 */
	private static class StaticSerializer<T>
		implements SerializerResolver<T>
	{
		private final Serializer<T> serializer;

		public StaticSerializer(Serializer<T> serializer)
		{
			this.serializer = serializer;
		}
		
		@Override
		public Serializer<T> find(TypeEncounter encounter)
		{
			return serializer;
		}
	}
}
