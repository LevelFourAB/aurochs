package se.l4.aurochs.statelog;

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
	 * Get the data stored in this log.
	 * 
	 * @return
	 */
	LogData<T> data();
	
	/**
	 * Submit an entry to this state log. This method will return a future
	 * that will trigger when the submitted entry has been committed to the
	 * log.
	 * 
	 * <p>
	 * The returned log entry can not be used to retrieve data and is only
	 * intended to be used for retrieving metadata such as the log entry id.
	 * 
	 * @param entry
	 * @return
	 */
	CompletableFuture<LogEntry<T>> submit(T entry);
	
	/**
	 * Close this state log.
	 */
	@Override
	void close();
}
