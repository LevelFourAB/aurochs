package se.l4.aurochs.config.internal;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Path.Node;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import se.l4.aurochs.config.Config;
import se.l4.aurochs.config.ConfigException;
import se.l4.aurochs.config.ConfigKey;
import se.l4.aurochs.config.Value;
import se.l4.aurochs.config.internal.streaming.MapInput;
import se.l4.aurochs.serialization.Serializer;
import se.l4.aurochs.serialization.SerializerCollection;
import se.l4.aurochs.serialization.WrappedSerializerCollection;
import se.l4.aurochs.serialization.format.StreamingInput;

import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

/**
 * Default implementation of {@link Config}.
 * 
 * @author Andreas Holstenson
 *
 */
public class DefaultConfig
	implements Config
{
	private final SerializerCollection collection;
	private final Map<String, Object> data;
	private final ValidatorFactory validatorFactory;
	
	public DefaultConfig(SerializerCollection collection, 
			ValidatorFactory validatorFactory, 
			File root, 
			Map<String, Object> data)
	{
		this.collection = new WrappedSerializerCollection(collection);
		this.validatorFactory = validatorFactory;
		this.data = data;
		
		collection.bind(File.class, new FileSerializer(root));
		collection.bind(ConfigKey.class, new ConfigKey.ConfigKeySerializer(this));
	}
	
	private static final Pattern LIST_GET = Pattern.compile("(.+)(?:\\[([0-9]+)\\])+");
	
	private String get(String path, int start, int end)
	{
		if(path.charAt(start) == '"') start += 1;
		if(path.charAt(end - 1) == '"') end -= 1;
		
		return path.substring(start, end);
	}
	
	private Object get(String path)
	{
		if(path == null || path.equals(""))
		{
			return data;
		}
		
		List<String> parts = Lists.newArrayList();
		boolean quoted = false;
		int lastIndex = 0;
		for(int i=0, n=path.length(); i<n; i++)
		{
			char c = path.charAt(i);
			if(! quoted && c == '.')
			{
				parts.add(get(path, lastIndex, i));
				lastIndex = i + 1;
			}
			
			if(c == '"')
			{
				quoted = ! quoted;
			}
		}
		
		parts.add(get(path, lastIndex, path.length()));
		
		Map<String, Object> current = data;
		for(int i=0, n=parts.size(); i<n; i++)
		{
			String part = parts.get(i);
			Matcher listGetMatcher = LIST_GET.matcher(part);
			if(listGetMatcher.matches())
			{
				String name = listGetMatcher.group(1);
				Object o = current.get(name);
				if(o == null)
				{
					return null;
				}
				
				if(! (o instanceof List))
				{
					String subPath = Joiner
						.on('.')
						.join(parts.subList(i+1, parts.size()));
					
					throw new ConfigException("Expected list at `" + subPath + "` but got: " + o);
				}
				
				List currentList = (List) o;
				
				String[] indexes = listGetMatcher.group(2).split(" ");
				for(int j=0, m=indexes.length; j<m; j++)
				{
					int idx = Integer.parseInt(indexes[j]);
					if(currentList.size() <= idx)
					{
						String subPath = Joiner
							.on('.')
							.join(parts.subList(i+1, parts.size()));
						
						throw new ConfigException("Expected list at `" + subPath + "` to contain at least " + (idx+1) + " values");
					}
					
					o = currentList.get(idx);
					if(o instanceof List && j<m-1)
					{
						currentList = (List) o;
					}
				}
				
				if(i == n-1)
				{
					return o;
				}
				else if(o instanceof Map)
				{
					current = (Map) o;
				}
				else
				{
					String subPath = Joiner
						.on('.')
						.join(parts.subList(i+1, parts.size()));
					
					throw new ConfigException("Expected several values at `" + subPath + "` but only got a single value: " + o);
				}
			}
			else if(current.containsKey(part))
			{
				if(i == n-1)
				{
					// Last part of path, return the value
					return current.get(part);
				}
				
				Object o = current.get(part);
				if(o instanceof Map)
				{
					current = (Map) o;
				}
				else
				{
					// TODO: Proper error message
					String subPath = Joiner
						.on('.')
						.join(parts.subList(i+1, parts.size()));
					
					throw new ConfigException("Expected several values at `" + subPath + "` but only got a single value: " + o);
				}
			}
			else
			{
				return null;
			}
		}
		
		return current.get(parts.get(parts.size() - 1));
	}

	@Override
	public <T> T asObject(String path, Class<T> type)
	{
		return get(path, type).getOrDefault(null);
	}
	
	@Override
	public Collection<String> keys(String path)
	{
		if(path == null || path.equals(""))
		{
			return data.keySet();
		}
			
		String[] parts = path.split("\\.");
		Map<String, Object> current = data;
		for(int i=0, n=parts.length; i<n; i++)
		{
			if(current.containsKey(parts[i]))
			{
				Object o = current.get(parts[i]);
				if(o instanceof Map)
				{
					current = (Map) o;
				}
				else
				{
					return Collections.emptyList();
				}
			}
			else
			{
				return Collections.emptyList();
			}
		}
			
		return Collections2.filter(current.keySet(), (s) -> ! s.startsWith("_aurochs_:"));
	}
	
	private void validateInstance(String path, Object object)
	{
		Validator validator = validatorFactory.getValidator();
		Set<ConstraintViolation<Object>> violations = validator.validate(object);
		
		if(violations.isEmpty())
		{
			// No violations
			return;
		}
		
		StringBuilder builder = new StringBuilder("Validation failed for `" + path + "`:\n");
		
		for(ConstraintViolation<Object> violation : violations)
		{
			builder
				.append("* ")
				.append(join(violation.getPropertyPath()))
				.append(violation.getMessage())
				.append("\n");
		}
		
		throw new ConfigException(builder.toString());
	}
	
	private String join(Path path)
	{
		StringBuilder builder = new StringBuilder();
		for(Node node : path)
		{
			if(builder.length() > 0)
			{
				builder.append(".");
			}
			
			builder.append(node.getName());
		}
		
		if(builder.length() > 0)
		{
			builder.append(": ");
		}
		
		return builder.toString();
	}
	
	@Override
	public <T> Value<T> get(String path, Class<T> type)
	{
		Serializer<T> serializer = collection.find(type);
		if(serializer == null)
		{
			throw new ConfigException("Unable to find a serializer suitable for " + type);
		}
		
		Object data = get(path);
		if(data == null)
		{
			return new ValueImpl<T>(path, false, null);
		}
		
		StreamingInput input = MapInput.resolveInput(path, data);
		try
		{
			T instance = serializer.read(input);
			
			validateInstance(path, instance);
			
			return new ValueImpl<T>(path, true, instance);
		}
		catch(ConfigException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			throw new ConfigException("Unable to create " + type + " from data at `" + path + "`; " + e.getMessage(), e);
		}
	}
	
	private static class ValueImpl<T>
		implements Value<T>
	{
		private final String path;
		private final boolean exists;
		private final T instance;

		public ValueImpl(String path, boolean exists, T instance)
		{
			this.path = path;
			this.exists = exists;
			this.instance = instance;
		}

		@Override
		public T get()
		{
			if(! exists)
			{
				throw new ConfigException("Unable to get config value at `" + path + "`");
			}
			return instance;
		}
		
		@Override
		public T getOrDefault(T defaultInstance)
		{
			return exists ? instance : defaultInstance;
		}
		
		@Override
		public boolean exists()
		{
			return exists;
		}
	}
}
