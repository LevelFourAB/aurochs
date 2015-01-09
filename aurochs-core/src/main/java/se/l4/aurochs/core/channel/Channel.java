package se.l4.aurochs.core.channel;

import java.util.concurrent.Executor;

import com.google.common.base.Predicate;

/**
 * Channel where messages can be sent and received.
 * 
 * @author Andreas Holstenson
 *
 * @param <T>
 */
public interface Channel<T>
	extends AutoCloseable
{
	/**
	 * Add a listener to this channel. The listener will be notified when
	 * events occur.
	 * 
	 * @param listener
	 */
	void addListener(ChannelListener<T> listener);
	
	/**
	 * Remove a listener from this channel. The listener will be notified when
	 * event occur.
	 * 
	 * @param listener
	 */
	void removeListener(ChannelListener<T> listener);
	
	/**
	 * Send a message over the channel.
	 * 
	 * @param message
	 */
	void send(T message);
	
	/**
	 * Filter this channel on the type of message.
	 * 
	 * @param type
	 * @return
	 */
	<O> Channel<O> filter(Class<O> type);
	
	/**
	 * Filter this channel with the specified {@link Predicate predicate}.
	 * 
	 * @param predicate
	 * @return
	 */
	Channel<T> filter(Predicate<T> predicate);
	
	/**
	 * Run all events on the specified executor.
	 * 
	 * @param executor
	 * @return
	 */
	Channel<T> on(Executor executor);
	
	/**
	 * Close this channel.
	 * 
	 */
	@Override
	void close();
}
