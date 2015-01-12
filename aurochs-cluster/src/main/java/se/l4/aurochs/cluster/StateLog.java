package se.l4.aurochs.cluster;

import java.util.concurrent.CompletableFuture;

public interface StateLog<T>
{
	/**
	 * Submit an entry to this state log. This method will return a future
	 * that will trigger when submitted entry has been committed to the log.
	 * 
	 * @param entry
	 * @return
	 */
	CompletableFuture<Void> submit(T entry);
}
