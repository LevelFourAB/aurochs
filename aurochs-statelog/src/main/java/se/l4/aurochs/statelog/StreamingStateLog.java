package se.l4.aurochs.statelog;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;


/**
 * A {@link StateLog} that works on multiple objects, applying all of the
 * entries at once. This allows a producer to stream objects to the log
 * while consumers will see all entries at once.
 * 
 * @author Andreas Holstenson
 *
 */
public interface StreamingStateLog<T>
	extends StateLog<Iterator<T>>
{
	/**
	 * Start a new operation to store several items.
	 * 
	 * @return
	 */
	StateLogStoreOperation<T> store();
	
	@Override
	default CompletableFuture<LogEntry<Iterator<T>>> submit(Iterator<T> entry)
	{
		// TODO: Implement this method
		throw new UnsupportedOperationException();
	}
}
