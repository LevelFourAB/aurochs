package se.l4.aurochs.serialization;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import se.l4.aurochs.serialization.collections.ArraySerializerResolver;
import se.l4.aurochs.serialization.collections.ListSerializerResolver;
import se.l4.aurochs.serialization.enums.EnumSerializerResolver;
import se.l4.aurochs.serialization.internal.TypeEncounterImpl;
import se.l4.aurochs.serialization.spi.DefaultInstanceFactory;
import se.l4.aurochs.serialization.spi.InstanceFactory;
import se.l4.aurochs.serialization.spi.SerializerResolver;
import se.l4.aurochs.serialization.spi.Type;
import se.l4.aurochs.serialization.spi.TypeEncounter;
import se.l4.aurochs.serialization.spi.TypeViaClass;
import se.l4.aurochs.serialization.standard.BooleanSerializer;
import se.l4.aurochs.serialization.standard.DoubleSerializer;
import se.l4.aurochs.serialization.standard.FloatSerializer;
import se.l4.aurochs.serialization.standard.IntSerializer;
import se.l4.aurochs.serialization.standard.LongSerializer;
import se.l4.aurochs.serialization.standard.ShortSerializer;
import se.l4.aurochs.serialization.standard.StringSerializer;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.ComputationException;
import com.google.common.collect.MapMaker;
import com.google.common.primitives.Primitives;

/**
 * Default implementation of {@link SerializerCollection}.
 * 
 * @author Andreas Holstenson
 *
 */
public class DefaultSerializerCollection
	implements SerializerCollection
{
	private static final SerializerResolver<?> ARRAY_RESOLVER = new ArraySerializerResolver();;
	
	private final InstanceFactory instanceFactory;
	private final Map<Class<?>, SerializerResolver<?>> boundTypeToResolver;
	private final Map<Class<?>, SerializerResolver<?>> typeToResolverCache;
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
		typeToResolverCache = new MapMaker()
			.makeComputingMap(new Function<Class<?>, SerializerResolver<?>>()
			{
				@Override
				public SerializerResolver<?> apply(Class<?> from)
				{
					return findOrCreateSerializerResolver(from);
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
		
		// Collections
		bind(List.class, new ListSerializerResolver());
		
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
		try
		{
			SerializerResolver finder = typeToResolverCache.get(Primitives.wrap(type.getErasedType()));
			
			Serializer serializer = finder.find(new TypeEncounterImpl(this, type, hints));
			if(serializer == null)
			{
				throw new SerializationException("Unable to find serializer for " + type);
			}
			
			return serializer;
		}
		catch(ComputationException e)
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
	
	protected SerializerResolver<?> findOrCreateSerializerResolver(Class<?> from)
	{
		Serializer<?> serializer = createSerializer(from);
		if(serializer != null)
		{
			registerIfNamed(from, serializer);
		
			return new StaticSerializer(serializer);
		}
		
		if(from.isArray())
		{
			// Arrays have special treatment, always use the array resolver
			return ARRAY_RESOLVER;
		}
		
		SerializerResolver<?> resolver = findSerializerResolver(from);
		if(resolver != null)
		{
			return resolver;
		}
		
		throw new SerializationException("Unable to find a suitable serializer for " + from);
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
	
	protected Serializer<?> createSerializer(Class<?> from)
	{
		if(from.isAnnotationPresent(Use.class))
		{
			// A specific serializer should be used
			Use annotation = from.getAnnotation(Use.class);
			Class<? extends Serializer> serializer = annotation.value();
			if(serializer == ReflectionSerializer.class)
			{
				return ReflectionSerializer.create(from, this);
			}
			
			return instanceFactory.create(annotation.value());
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
