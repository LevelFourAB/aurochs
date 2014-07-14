package se.l4.aurochs.core.internal;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Set;

import org.reflections.Reflections;

import se.l4.aurochs.core.AutoLoad;
import se.l4.aurochs.core.AutoLoader;

import com.google.common.collect.ImmutableSet;

public class AutoLoaderImpl
	implements AutoLoader
{
	private final Reflections reflections;

	public AutoLoaderImpl(Collection<String> packageNamesToScan)
	{
		reflections = new Reflections(packageNamesToScan.toArray());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> Set<Class<? extends T>> getPluginsOfType(Class<T> type)
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
	public Set<Class<?>> getClassesAnnotatedWith(Class<? extends Annotation> annotationType)
	{
		return reflections.getTypesAnnotatedWith(annotationType);
	}
}
