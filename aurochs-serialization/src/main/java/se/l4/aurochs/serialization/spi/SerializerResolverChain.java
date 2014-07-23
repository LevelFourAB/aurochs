package se.l4.aurochs.serialization.spi;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Set;

import se.l4.aurochs.serialization.Serializer;

import com.google.common.collect.ImmutableSet;

public class SerializerResolverChain<T>
	implements SerializerResolver<T>
{
	private final SerializerResolver<T>[] resolvers;
	private final Set<Class<? extends Annotation>> hints;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public SerializerResolverChain(Collection<SerializerResolver<T>> resolvers)
	{
		int i = 0;
		ImmutableSet.Builder<Class<? extends Annotation>> builder = ImmutableSet.builder();
		SerializerResolver[] resolverArray = new SerializerResolver[resolvers.size()];
		for(SerializerResolver<?> r : resolvers)
		{
			resolverArray[i++] = r;
			builder.addAll(r.getHints());
		}
		
		this.hints = builder.build();
		this.resolvers = resolverArray;
	}
	
	@Override
	public Serializer<T> find(TypeEncounter encounter)
	{
		for(SerializerResolver<T> resolver : resolvers)
		{
			Serializer<T> serializer = resolver.find(encounter);
			if(serializer != null) return serializer;
		}
		
		return null;
	}
	
	@Override
	public Set<Class<? extends Annotation>> getHints()
	{
		return hints;
	}
}
