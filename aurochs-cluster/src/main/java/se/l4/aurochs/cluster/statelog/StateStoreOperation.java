package se.l4.aurochs.cluster.statelog;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;

/**
 * An operation to store a stream in the log.
 * 
 * @author Andreas Holstenson
 *
 */
public interface StateStoreOperation
{
	/**
	 * Get the stream to write data to.
	 * 
	 * @return
	 */
	OutputStream stream();
	
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