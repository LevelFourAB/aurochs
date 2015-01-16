package se.l4.aurochs.cluster.internal.raft.log;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import se.l4.aurochs.cluster.internal.raft.Raft;
import se.l4.aurochs.core.io.Bytes;

import com.google.common.collect.Lists;

/**
 * A log as used with {@link Raft}.
 * 
 * @author Andreas Holstenson
 *
 */
public interface Log
	extends Closeable
{
	/**
	 * Get the first index stored in the log, or {@code 0} if no entries.
	 * 
	 * @return
	 */
	long first();
	
	/**
	 * Get the last index stored in the log, or {@code 0} if no entries.
	 * 
	 * @return
	 */
	long last();
	
	/**
	 * Get a specific entry.
	 * 
	 * @param id
	 * @return
	 */
	LogEntry get(long index)
		throws IOException;
	
	/**
	 * Store a specific entry.
	 * 
	 * @param data
	 * @return
	 */
	long store(long term, LogEntry.Type type, Bytes data)
		throws IOException;
	
	/**
	 * Reset the log to the given index.
	 * 
	 * @param index
	 * @throws IOException
	 */
	void resetTo(long index)
		throws IOException;
	
//	/**
//	 * Store several log entries at the same time, returning their identifiers.
//	 * 
//	 * @param data
//	 * @return
//	 * @throws IOException
//	 */
//	default long[] store(List<Bytes> data)
//		throws IOException
//	{
//		long[] result = new long[data.size()];
//		int i = 0;
//		for(Bytes b : data)
//		{
//			result[i++] = store(b);
//		}
//		return result;
//	}
	
	/**
	 * Load several log entries at the same time, returning their identifiers.
	 * 
	 * @param ids
	 * @return
	 * @throws IOException
	 */
	default List<LogEntry> get(long[] ids)
		throws IOException
	{
		List<LogEntry> result = Lists.newArrayListWithCapacity(ids.length);
		for(long id : ids)
		{
			result.add(get(id));
		}
		return result;
	}
}
