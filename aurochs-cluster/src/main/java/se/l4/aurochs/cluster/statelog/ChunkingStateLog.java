package se.l4.aurochs.cluster.statelog;

import se.l4.aurochs.cluster.StateLog;

/**
 * A version of a {@link StateLog} that is designed to store large streams.
 * 
 * @author Andreas Holstenson
 *
 */
public interface ChunkingStateLog
	extends AutoCloseable
{
	/**
	 * Start storing a new chunk in the log.
	 * 
	 * @return
	 */
	StateStoreOperation store();
	
	/**
	 * Close this log.
	 * 
	 */
	@Override
	void close();
}
