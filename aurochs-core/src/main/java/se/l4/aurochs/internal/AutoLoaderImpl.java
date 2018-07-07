package se.l4.aurochs.internal;

import java.util.Set;
import java.util.stream.Collectors;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.reflections.Reflections;

import se.l4.aurochs.AutoLoad;
import se.l4.aurochs.AutoLoader;
import se.l4.commons.types.InstanceFactory;
import se.l4.commons.types.internal.TypeFinderOverReflections;

@Singleton
public class AutoLoaderImpl
	extends TypeFinderOverReflections
	implements AutoLoader
{
	@Inject
	public AutoLoaderImpl(InstanceFactory instanceFactory, Reflections reflections)
	{
		super(instanceFactory, reflections);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T> Set<Class<? extends T>> getAutoLoadedClassesOfType(Class<T> type)
	{
		return (Set) getTypesAnnotatedWith(AutoLoad.class)
			.stream()
			.filter(t -> type.isAssignableFrom(t))
			.collect(Collectors.toSet());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T> Set<? extends T> getAutoLoadedInstancesOfType(Class<T> type)
	{
		return (Set) getTypesAnnotatedWithAsInstances(AutoLoad.class)
			.stream()
			.filter(t -> type.isAssignableFrom(t.getClass()))
			.collect(Collectors.toSet());
	}
	
}
