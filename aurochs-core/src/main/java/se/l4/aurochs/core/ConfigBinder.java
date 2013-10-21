package se.l4.aurochs.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import se.l4.aurochs.config.Config;
import se.l4.aurochs.config.Value;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.util.Types;

/**
 * Binder to help with binding configuration values.
 *  
 * @author Andreas Holstenson
 *
 */
public class ConfigBinder
{
	public interface BindingBuilder<T>
	{
		BindingBuilder<T> annotatedWith(Class<? extends Annotation> annotation);
		
		BindingBuilder<T> annotatedWith(Annotation annotation);
		
		BindingBuilder<T> withDefault(T value);
		
		ConfigBinder to(String path);
	}
	
	private final Binder binder;

	private ConfigBinder(Binder binder)
	{
		this.binder = binder.skipSources(ConfigBinder.class, BindingBuilderImpl.class, Definition.class);
	}
	
	/**
	 * Create a new binder.
	 * 
	 * @param binder
	 * @return
	 */
	public static ConfigBinder newBinder(Binder binder)
	{
		return new ConfigBinder(binder);
	}
	
	/**
	 * Bind the specified type to a configuration value.
	 * 
	 * @param key
	 * @param type
	 * @return
	 */
	public <T> BindingBuilder<T> bind(final Class<T> type)
	{
		return new BindingBuilderImpl<T>(this, type);
	}
	
	private static class BindingBuilderImpl<T>
		implements BindingBuilder<T>
	{
		private final ConfigBinder binder;
		private final Class<T> type;
		
		private Annotation annotation;
		private Class<? extends Annotation> annotationClass;
		
		private T defaultValue;
		
		public BindingBuilderImpl(ConfigBinder binder, Class<T> type)
		{
			this.binder = binder;
			this.type = type;
		}
		
		@Override
		public ConfigBinder to(String path)
		{
			new Definition(path, type, annotationClass, annotation, defaultValue).bind(binder.binder);
			
			return binder;
		}
		
		@Override
		public BindingBuilder<T> annotatedWith(Annotation annotation)
		{
			this.annotation = annotation;
			
			return this;
		}
		
		@Override
		public BindingBuilder<T> annotatedWith(Class<? extends Annotation> annotation)
		{
			this.annotationClass = annotation;
			
			return this;
		}
		
		@Override
		public BindingBuilder<T> withDefault(T value)
		{
			this.defaultValue = value;
			
			return this;
		}
	};
	
	private static class Definition<T>
	{
		private String key;
		private Class<T> type;
		
		private Class<? extends Annotation> annotationClass;
		private Annotation annotation;
		
		private T defaultValue;
		
		public Definition(String key, Class<T> type, Class<? extends Annotation> annotationClass, Annotation annotation, T defaultValue)
		{
			this.key = key;
			this.type = type;
			this.annotationClass = annotationClass;
			this.annotation = annotation;
			this.defaultValue = defaultValue;
		}
		
		public void bind(Binder binder)
		{
			
			Type valueType = Types.newParameterizedType(Value.class, type);
			Key<Value<T>> valueKey;
			Key<T> key;

			if(annotationClass != null)
			{
				valueKey = (Key) Key.get(valueType, annotationClass);
				key = Key.get(type, annotationClass);
			}
			else if(annotation != null)
			{
				valueKey = (Key) Key.get(valueType, annotation);
				key = Key.get(type, annotation);
			}
			else
			{
				valueKey = (Key) Key.get(valueType);
				key = Key.get(type);
			}
			
			Provider<Value<T>> provider = createProvider(binder);
			
			binder.bind(valueKey)
				.toProvider(provider)
				.in(Scopes.SINGLETON);
			
			binder.bind(key)
				.toProvider(new ConfigObjectProvider<T>(provider, defaultValue));
		}

		private Provider<Value<T>> createProvider(Binder binder)
		{
			Provider<Config> config = binder.getProvider(Config.class);
			return new ConfigValueProvider<T>(config, key, type);
		}
	}
	
	private static class ConfigObjectProvider<T>
		implements Provider<T>
	{
		private final Provider<Value<T>> provider;
		private final T defaultValue;

		public ConfigObjectProvider(Provider<Value<T>> provider, T defaultValue)
		{
			this.provider = provider;
			this.defaultValue = defaultValue;
		}
		
		@Override
		public T get()
		{
			return provider.get().getOrDefault(defaultValue);
		}
	}
	
	private static class ConfigValueProvider<T>
		implements Provider<Value<T>>
	{
		private final Provider<Config> config;
		private final String key;
		private final Class<T> type;
		
		public ConfigValueProvider(Provider<Config> config, String key, Class<T> type)
		{
			this.config = config;
			this.key = key;
			this.type = type;
		}
		
		@Override
		public Value<T> get()
		{
			return config.get().get(key, type);
		}
	}
}
