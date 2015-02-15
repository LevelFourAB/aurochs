package se.l4.aurochs.serialization;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import se.l4.aurochs.serialization.internal.DelayedSerializer;
import se.l4.aurochs.serialization.internal.TypeEncounterImpl;
import se.l4.aurochs.serialization.spi.SerializerResolver;
import se.l4.aurochs.serialization.spi.StaticSerializerResolver;
import se.l4.aurochs.serialization.spi.Type;
import se.l4.aurochs.serialization.spi.TypeViaClass;

import com.google.common.collect.Lists;

/**
 * Default implementation of {@link SerializerCollection}.
 * 
 * @author Andreas Holstenson
 *
 */
public abstract class AbstractSerializerCollection
	implements SerializerCollection
{
	private static final ThreadLocal<Set<Type>> stack = new ThreadLocal<Set<Type>>();
	
	private final Map<QualifiedName, Serializer<?>> nameToSerializer;
	private final Map<Serializer<?>, QualifiedName> serializerToName;
	private final Map<CacheKey, Serializer<?>> serializers;
	
	public AbstractSerializerCollection()
	{
		nameToSerializer = new ConcurrentHashMap<QualifiedName, Serializer<?>>();
		serializerToName = new ConcurrentHashMap<Serializer<?>, QualifiedName>();
		serializers = new ConcurrentHashMap<>();
	}
	
	protected <T> void bind(Class<T> type, Serializer<T> serializer, String ns, String name)
	{
		bind(type, new StaticSerializerResolver<T>(serializer));
		
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
		bind(type, new StaticSerializerResolver<T>(serializer));
		
		registerIfNamed(type, serializer);
		
		return this;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> Serializer<T> find(Class<T> type)
	{
		return (Serializer<T>) find(new TypeViaClass(type));
	}
	
	@Override
	@SuppressWarnings("unchecked")
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
	@SuppressWarnings({ "rawtypes" })
	public Serializer<?> find(Type type, Annotation... hints)
	{
		Set<Type> s = stack.get();
		if(s != null && s.contains(type))
		{
			// Already trying to create this serializer, delay creation
			return new DelayedSerializer(this, type, hints);
		}
		
		// Locate the resolver to use
		SerializerResolver finder = getResolver(type.getErasedType());
		if(finder == null)
		{
			throw new SerializationException("Unable to retreive serializer for " + type + "; Type does not appear serializable");
		}
		
		return createVia(finder, type, hints);
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
	public <T> Serializer<T> findVia(Class<? extends SerializerOrResolver<T>> resolver, Class<T> type, Annotation... hints)
	{
		return findVia(resolver, new TypeViaClass(type), hints);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T> Serializer<T> findVia(Class<? extends SerializerOrResolver<T>> resolver, Type type, Annotation... hints)
	{
		SerializerOrResolver<T> instance = getInstanceFactory().create(resolver);
		if(instance instanceof Serializer)
		{
			return (Serializer<T>) instance;
		}
		else
		{
			return (Serializer) createVia((SerializerResolver) instance, type, hints);
		}
	}
	
	/**
	 * Create a new {@link Serializer} for the given type and hints via a
	 * specific {@link SerializerResolver resolver} instance.
	 * 
	 * @param resolver
	 * @param type
	 * @param hints
	 * @return
	 */
	protected Serializer<?> createVia(SerializerResolver<?> resolver, Type type, Annotation... hints)
	{
		Set<Type> s = stack.get();
		
		// Only expose the hints that the resolver has declared
		Set<Class<? extends Annotation>> hintsUsed = resolver.getHints();
		List<Annotation> hintsActive;
		if(hintsUsed == null || hints == null || hints.length == 0)
		{
			hintsActive = Collections.emptyList();
		}
		else
		{
			hintsActive = Lists.newArrayList();
			for(Annotation a : hints)
			{
				if(hintsUsed.contains(a.annotationType()))
				{
					hintsActive.add(a);
				}
			}
		}
		
		// Check if we have already built a serializer for this type
		CacheKey key = new CacheKey(type, hintsActive.toArray());
		Serializer<?> serializer = serializers.get(key);
		if(serializer != null)
		{
			return serializer;
		}
		
		// Stack to keep track of circular dependencies
		if(s == null)
		{
			s = new HashSet<Type>();
			stack.set(s);
		}
		
		try
		{
			s.add(type);
			
			// Find a serializer to use
			TypeEncounterImpl encounter = new TypeEncounterImpl(this, type, hintsActive);
			
			serializer = resolver.find(encounter);
			if(serializer == null)
			{
				throw new SerializationException("Unable to find serializer for " + type + " using " + resolver.getClass());
			}
			
			registerIfNamed(type.getErasedType(), serializer);
			
			// Store the found serializer in the cache
			serializers.put(key, serializer);
			return serializer;
		}
		finally
		{
			s.remove(type);
			
			if(s.isEmpty())
			{
				stack.remove();
			}
		}
	}
	
	@Override
	public QualifiedName findName(Serializer<?> serializer)
	{
		return serializerToName.get(serializer);
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public boolean isSupported(Class<?> type)
	{
		SerializerResolver finder = getResolver(type);
		if(finder == null) return false;
		
		if(finder instanceof StaticSerializerResolver)
		{
			return true;
		}
		
		Serializer serializer = finder.find(new TypeEncounterImpl(this, new TypeViaClass(type), null));
		return serializer != null;
	}
	
	/**
	 * Register the given serializer if it has a name.
	 * 
	 * @param from
	 * @param serializer
	 */
	protected void registerIfNamed(Class<?> from, Serializer<?> serializer)
	{
		if(from.isAnnotationPresent(Named.class))
		{
			Named named = from.getAnnotation(Named.class);
			QualifiedName key = new QualifiedName(named.namespace(), named.name());
			nameToSerializer.put(key, serializer);
			serializerToName.put(serializer, key);
		}
	}

	private static class CacheKey
	{
		private final Type type;
		private final Object[] hints;

		public CacheKey(Type type, Object[] hints)
		{
			this.type = type;
			this.hints = hints;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(hints);
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if(this == obj)
				return true;
			if(obj == null)
				return false;
			if(getClass() != obj.getClass())
				return false;
			CacheKey other = (CacheKey) obj;
			if(!Arrays.equals(hints, other.hints))
				return false;
			if(type == null)
			{
				if(other.type != null)
					return false;
			}
			else if(!type.equals(other.type))
				return false;
			return true;
		}
	}
}
