package se.l4.aurochs.config;

import java.util.Collection;


public interface Config
{
	/**
	 * Resolve values as the given path as an object. This is equivalent
	 * to call {@link #get(String, Class)} and then {@link Value#getOrDefault()}
	 * with the value {@code null}.
	 * 
	 * @param path
	 * @param type
	 * @return
	 */
	<T> T asObject(String path, Class<T> type);
	
	/**
	 * Resolve configuration values as an object. The object will be created
	 * via serialization.
	 * 
	 * @param path
	 * @param type
	 * @return
	 */
	<T> Value<T> get(String path, Class<T> type);
	
	/**
	 * Get the direct subkeys of the given path.
	 * 
	 * @param path
	 * @return
	 */
	Collection<String> keys(String path);
}
