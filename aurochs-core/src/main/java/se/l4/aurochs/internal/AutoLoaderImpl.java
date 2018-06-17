package se.l4.aurochs.internal;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.stream.Collectors;

import org.reflections.Reflections;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

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
	public AutoLoaderImpl(Injector injector, Reflections reflections)
	{
		super(createInstanceFactory(injector), reflections);
	}
	
	private static InstanceFactory createInstanceFactory(Injector injector)
	{
		return new InstanceFactory()
		{
			@Override
			public <T> T create(Class<T> type, Annotation[] annotations)
			{
				throw new UnsupportedOperationException();
			}
			
			@Override
			public <T> T create(Class<T> type)
			{
				return injector.getInstance(type);
			}
		};
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
