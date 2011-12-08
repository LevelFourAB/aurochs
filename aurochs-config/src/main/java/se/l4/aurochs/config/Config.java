package se.l4.aurochs.config;


public interface Config
{
	/**
	 * Resolve values as the given path as an object. This is equivalent
	 * to call {@link #get(String, Class)} and then {@link Value#get()}.
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
}
