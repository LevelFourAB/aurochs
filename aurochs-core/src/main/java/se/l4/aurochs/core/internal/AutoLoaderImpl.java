package se.l4.aurochs.core.internal;

import java.lang.annotation.Annotation;
import java.util.Set;

import org.reflections.Reflections;

import se.l4.aurochs.core.AutoLoad;
import se.l4.aurochs.core.AutoLoader;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

@Singleton
public class AutoLoaderImpl
	implements AutoLoader
{
	private final Injector injector;
	private final Reflections reflections;

	@Inject
	public AutoLoaderImpl(Injector injector, Reflections reflections)
	{
		this.injector = injector;
		this.reflections = reflections;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> Set<Class<? extends T>> getPluginsOfType(Class<T> type)
	{
		return getAutoLoadedClassesOfType(type);
	}
	
	@Override
	public <T> Set<Class<? extends T>> getAutoLoadedClassesOfType(Class<T> type)
	{
		Set<Class<?>> allPlugins = reflections.getTypesAnnotatedWith(AutoLoad.class);
		ImmutableSet.Builder<Class<? extends T>> builder = ImmutableSet.builder();
		for(Class<?> c : allPlugins)
		{
			if(type.isAssignableFrom(c))
			{
				builder.add((Class<? extends T>) c);
			}
		}
		return builder.build();
	}
	
	@Override
	public <T> Set<? extends T> getAutoLoadedInstancesOfType(Class<T> type)
	{
		ImmutableSet.Builder<T> builder = ImmutableSet.builder();
		for(Class<? extends T> t : getAutoLoadedClassesOfType(type))
		{
			builder.add(injector.getInstance(t));
		}
		return builder.build();
	}
	
	@Override
	public Set<Class<?>> getClassesAnnotatedWith(Class<? extends Annotation> annotationType)
	{
		return reflections.getTypesAnnotatedWith(annotationType);
	}
	
	@Override
	public <T> Set<Class<? extends T>> getSubTypesOf(Class<T> type)
	{
		return reflections.getSubTypesOf(type);
	}

	@Override
	public <T> Set<? extends T> getSubTypesAsInstances(Class<T> type)
	{
		ImmutableSet.Builder<T> builder = ImmutableSet.builder();
		for(Class<? extends T> t : getSubTypesOf(type))
		{
			builder.add(injector.getInstance(t));
		}
		return builder.build();
	}
}
