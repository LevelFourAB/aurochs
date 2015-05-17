package se.l4.aurochs.core.log;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * An operation to store a stream in the log.
 * 
 * @author Andreas Holstenson
 *
 */
public interface StateLogStoreOperation<T>
{
	/**
	 * Store the specified item.
	 * 
	 * @param item
	 */
	void store(T item)
		throws IOException;
	
	/**
	 * Abort this operation.
	 * 
	 * @throws IOException
	 */
	void abort()
		throws IOException;
	
	/**
	 * Submit this operation.
	 * 
	 * @return
	 */
	CompletableFuture<Void> commit();
}