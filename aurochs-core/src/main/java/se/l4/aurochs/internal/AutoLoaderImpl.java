package se.l4.aurochs.internal;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import se.l4.aurochs.AutoLoad;
import se.l4.aurochs.AutoLoader;
import se.l4.commons.types.TypeFinder;

@Singleton
public class AutoLoaderImpl
	implements AutoLoader
{
	private final TypeFinder typeFinder;

	@Inject
	public AutoLoaderImpl(TypeFinder typeFinder)
	{
		this.typeFinder = typeFinder;
	}

	@Override
	public <T> Set<? extends T> getSubTypesAsInstances(Class<T> type)
	{
		return typeFinder.getSubTypesAsInstances(type);
	}

	@Override
	public <T> Set<Class<? extends T>> getSubTypesOf(Class<T> type)
	{
		return typeFinder.getSubTypesOf(type);
	}

	@Override
	public Set<Class<?>> getTypesAnnotatedWith(Class<? extends Annotation> annotationType)
	{
		return typeFinder.getTypesAnnotatedWith(annotationType);
	}

	@Override
	public Set<? extends Object> getTypesAnnotatedWithAsInstances(Class<? extends Annotation> annotationType)
	{
		return typeFinder.getTypesAnnotatedWithAsInstances(annotationType);
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
