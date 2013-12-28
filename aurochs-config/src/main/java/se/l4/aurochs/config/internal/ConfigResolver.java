package se.l4.aurochs.config.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.bval.jsr303.xml.GetterType;

import se.l4.aurochs.config.ConfigKey;
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
		resolve(newRoot, root, "");
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
		resolve(target, root, "");
		return target;
	}
	
	private static void resolve(Map<String, Object> newRoot, Map<String, Object> root, String currentKey)
	{
		for(Map.Entry<String, Object> e : root.entrySet())
		{
			String key = e.getKey();
			Object value = e.getValue();
			
			if(value instanceof Map)
			{
				Map<String, Object> map = new HashMap<String, Object>();
				
				String newKey = currentKey.isEmpty() ? key : currentKey + '.' + key;
				map.put(ConfigKey.NAME, newKey);
				
				resolve(map, (Map) value, newKey);
				store(newRoot, key, map, currentKey);
			}
			else if(value instanceof List)
			{
				List<Object> list = new ArrayList<Object>();
				
				String newKey = currentKey.isEmpty() ? key : currentKey + '.' + key;
				
				resolve(list, (List) value, newKey);
				store(newRoot, key, list, currentKey);
			}
			else
			{
				store(newRoot, key, value, currentKey);
			}
		}
	}
	
	private static void resolve(List<Object> newList, List<Object> oldList, String currentKey)
	{
		for(int i=0, n=oldList.size(); i<n; i++)
		{
			String newKey = currentKey + '[' + i + ']';
			Object value = oldList.get(i);
			if(value instanceof Map)
			{
				Map<String, Object> map = new HashMap<String, Object>();
				map.put(ConfigKey.NAME, newKey);
				
				resolve(map, (Map) value, newKey);
				value = map;
			}
			else if(value instanceof List)
			{
				List<Object> list = new ArrayList<Object>();
				resolve(list, (List) value, newKey);
				value = list;
			}
			
			newList.add(value);
		}
	}

	private static void store(Map<String, Object> root, String key, Object value, String currentKey)
	{
		Map<String, Object> current = root;
		if(key.startsWith("\""))
		{
			// Quoted key, do not resolve dots
			root.put(key.substring(1, key.length()-1), value);
			return;
		}
		
		String[] path = key.split("\\.");
		StringBuilder resolvedPath = new StringBuilder(currentKey);
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
				if(resolvedPath.length() > 0) resolvedPath.append('.');
				resolvedPath.append(path[i]);
				
				newMap.put(ConfigKey.NAME, resolvedPath.toString());
				current.put(path[i], newMap);
				current = newMap;
			}
		}
		
		current.put(path[path.length - 1], value);
	}
}
