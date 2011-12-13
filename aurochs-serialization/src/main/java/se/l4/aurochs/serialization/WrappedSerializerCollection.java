package se.l4.aurochs.serialization;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import se.l4.aurochs.serialization.internal.TypeEncounterImpl;
import se.l4.aurochs.serialization.spi.InstanceFactory;
import se.l4.aurochs.serialization.spi.SerializerResolver;
import se.l4.aurochs.serialization.spi.Type;
import se.l4.aurochs.serialization.spi.TypeEncounter;
import se.l4.aurochs.serialization.spi.TypeViaClass;

import com.google.common.primitives.Primitives;

/**
 * Implementation of {@link SerializerCollection} that wraps another
 * collection.
 * 
 * @author Andreas Holstenson
 *
 */
public class WrappedSerializerCollection
	implements SerializerCollection
{
	private final Map<Class<?>, SerializerResolver<?>> boundTypeToResolver;
	private final Map<Class<?>, SerializerResolver<?>> typeToResolverCache;
	private final Map<QualifiedName, Serializer<?>> nameToSerializer;
	private final Map<Serializer<?>, QualifiedName> serializerToName;

	private final SerializerCollection other;
	
	public WrappedSerializerCollection(SerializerCollection other)
	{
		this.other = other;
		
		nameToSerializer = new ConcurrentHashMap<QualifiedName, Serializer<?>>();
		serializerToName = new ConcurrentHashMap<Serializer<?>, QualifiedName>();
		
		boundTypeToResolver = new ConcurrentHashMap<Class<?>, SerializerResolver<?>>();
		typeToResolverCache = new ConcurrentHashMap<Class<?>, SerializerResolver<?>>();
	}
	
	@Override
	public InstanceFactory getInstanceFactory()
	{
		return other.getInstanceFactory();
	}
	
	@Override
	public SerializerCollection bind(Class<?> type)
	{
		other.bind(type);
		
		return this;
	}
	
	private <T> void bind(Class<T> type, Serializer<T> serializer, String ns, String name)
	{
		bind(type, new StaticSerializer<T>(serializer));
		
		QualifiedName qname = new QualifiedName(ns, name);
		nameToSerializer.put(qname, serializer);
		serializerToName.put(serializer, qname);
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
		typeToResolverCache.put(type, resolver);
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
		SerializerResolver finder = typeToResolverCache.get(Primitives.wrap(type.getErasedType()));
		if(finder == null)
		{
			return other.find(type, hints);
		}
			
		Serializer serializer = finder.find(new TypeEncounterImpl(this, type, hints));
		if(serializer == null)
		{
			return other.find(type, hints);
		}
			
		return serializer;
	}
	
	@Override
	public Serializer<?> find(String name)
	{
		return find("", name);
	}
	
	@Override
	public Serializer<?> find(String namespace, String name)
	{
		Serializer<?> serializer = nameToSerializer.get(new QualifiedName(namespace, name));
		if(serializer != null)
		{
			return serializer;
		}
		
		return other.find(namespace, name);
	}
	
	@Override
	public QualifiedName findName(Serializer<?> serializer)
	{
		QualifiedName qname = serializerToName.get(serializer);
		if(qname != null)
		{
			return qname;
		}
		
		return other.findName(serializer);
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
