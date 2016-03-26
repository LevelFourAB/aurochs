package se.l4.aurochs.cluster.internal.raft.log;

import se.l4.commons.io.Bytes;

public interface StoredLogEntry
{
	/**
	 * Type of the log entry, used to support log keeping of internal data.
	 * 
	 * @author Andreas Holstenson
	 *
	 */
	enum Type
	{
		/**
		 * Data as submitted by a client of the state log.
		 */
		DATA
	}
	
	/**
	 * Get the identifier of this entry.
	 * 
	 * @return
	 */
	long getIndex();
	
	/**
	 * Get the term this was stored.
	 * 
	 * @return
	 */
	long getTerm();
	
	/**
	 * Get the type of this entry.
	 * 
	 * @return
	 */
	Type getType();
	
	/**
	 * Get the data of this entry. The returned {@link Bytes} should be valid for usage as long
	 * as the {@link Log log} is open.
	 * 
	 * @return
	 */
	Bytes getData();
}
