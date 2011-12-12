package se.l4.aurochs.core;

import se.l4.aurochs.core.channel.Channel;

import com.google.inject.Key;

/**
 * Session within the application. A session can be remote or local and 
 * is used as a {@link Channel communication channel} between systems.
 * 
 * @author Andreas Holstenson
 *
 */
public interface Session
	extends Channel<Object>
{
	/**
	 * Use injection to retrieve an instance of an object. Use this method
	 * when the class being created depends on a session in any way.
	 * 
	 * @param instance
	 * @return
	 */
	<T> T get(Class<T> instance);
	
	/**
	 * Use injection to retrieve an instance, see {@link #get(Class)}.
	 * 
	 * @param key
	 * @return
	 */
	<T> T get(Key<T> key);
}
