package se.l4.aurochs.config.internal;

import java.util.HashMap;
import java.util.Map;

import se.l4.aurochs.serialization.SerializationException;

/**
 * Resolver for embedded references to other parts of the configuration.
 * 
 * @author Andreas Holstenson
 *
 */
public class ConfigResolver
{
	private ConfigResolver()
	{
	}
	
	/**
	 * Resolve the specified map and return the resolved map.
	 * 
	 * @param root
	 * @return
	 */
	public static Map<String, Object> resolve(Map<String, Object> root)
	{
		Map<String, Object> newRoot = new HashMap<String, Object>();
		resolve(newRoot, root);
		return newRoot;
	}

	/**
	 * Resolve the specified map and store it in the specified target.
	 * 
	 * @param root
	 * @param target
	 * @return
	 */
	public static Map<String, Object> resolveTo(Map<String, Object> root, Map<String, Object> target)
	{
		resolve(target, root);
		return target;
	}
	
	private static void resolve(Map<String, Object> newRoot, Map<String, Object> root)
	{
		for(Map.Entry<String, Object> e : root.entrySet())
		{
			String key = e.getKey();
			Object value = e.getValue();
			
			if(value instanceof Map)
			{
				Map<String, Object> map = new HashMap<String, Object>();
				resolve(map, (Map) value);
				store(newRoot, key, map);
			}
			else
			{
				store(newRoot, key, value);
			}
		}
	}

	private static void store(Map<String, Object> root, String key, Object value)
	{
		Map<String, Object> current = root;
		String[] path = key.split("\\.");
		for(int i=0, n=path.length-1; i<n; i++)
		{
			if(current.containsKey(path[i]))
			{
				Object o = current.get(path[i]);
				if(o instanceof Map)
				{
					current = (Map) o;
				}
				else
				{
					// TODO: Proper error message
					throw new SerializationException();
				}
			}
			else
			{
				Map<String, Object> newMap = new HashMap<String, Object>();
				current.put(path[i], newMap);
				current = newMap;
			}
		}
		
		current.put(path[path.length - 1], value);
	}
}
