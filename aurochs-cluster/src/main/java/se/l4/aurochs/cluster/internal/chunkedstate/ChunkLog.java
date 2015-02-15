package se.l4.aurochs.cluster.internal.chunkedstate;

import java.util.Iterator;

import com.carrotsearch.hppc.cursors.LongCursor;

public interface ChunkLog
{
	/**
	 * Store a chunk in the temporary storage.
	 * 
	 * @param chunk
	 */
	void storeChunk(Chunk chunk);
	
	/**
	 * Get all the chunks for a certain request.
	 * 
	 * @param request
	 * @return
	 */
	Iterator<Chunk> chunks(long request);
	
	/**
	 * Remove all chunks for the given request.
	 * 
	 * @param request
	 */
	void remove(long request);
	
	/**
	 * Get all the requests that 
	 * @return
	 */
	LongCursor uncommitedRequests();
}
