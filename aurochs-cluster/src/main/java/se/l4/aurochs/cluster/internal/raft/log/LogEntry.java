package se.l4.aurochs.cluster.internal.raft.log;

import se.l4.aurochs.core.io.Bytes;

public interface LogEntry
{
	/**
	 * Get the identifier of this entry.
	 * 
	 * @return
	 */
	long getId();
	
	/**
	 * Get the term this was stored.
	 * 
	 * @return
	 */
	long getTerm();
	
	/**
	 * Get the data of this entry. The returned {@link Bytes} should be valid for usage as long
	 * as the {@link Log log} is open.
	 * 
	 * @return
	 */
	Bytes getData();
}
