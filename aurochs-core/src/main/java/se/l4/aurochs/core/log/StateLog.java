package se.l4.aurochs.core.log;

import java.util.concurrent.CompletableFuture;

/**
 * 
 * @author Andreas Holstenson
 *
 * @param <T>
 */
public interface StateLog<T>
	extends AutoCloseable
{
	/**
	 * Submit an entry to this state log. This method will return a future
	 * that will trigger when the submitted entry has been committed to the
	 * log.
	 * 
	 * @param entry
	 * @return
	 */
	CompletableFuture<Void> submit(T entry);
	
	/**
	 * Close this state log.
	 */
	@Override
	void close();
}
