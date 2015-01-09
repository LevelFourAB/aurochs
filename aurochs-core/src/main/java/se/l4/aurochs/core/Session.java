package se.l4.aurochs.core;

import se.l4.aurochs.core.channel.Channel;
import se.l4.aurochs.core.io.ByteMessage;
import se.l4.aurochs.serialization.Named;
import se.l4.aurochs.serialization.SerializerCollection;

import com.google.inject.Key;

/**
 * Session within the application. A session can be remote or local and 
 * is used as a {@link Channel communication channel} between systems.
 * 
 * @author Andreas Holstenson
 *
 */
public interface Session
{
	/**
	 * Get a channel that can be used to communicate via {@link ByteMessage}s.
	 * 
	 * @return
	 */
	Channel<ByteMessage> getRawChannel();
	
	/**
	 * Get a channel that can be used to communicate via objects that are serializable via
	 * {@link SerializerCollection} and at the same time are {@link Named}.
	 * 
	 * @return
	 */
	Channel<Object> getObjectChannel();
	
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
