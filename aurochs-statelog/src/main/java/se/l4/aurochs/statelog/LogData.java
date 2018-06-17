package se.l4.aurochs.statelog;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * Data as stored in a {@link StateLog}, allows applications to inspect the
 * data in addition to acting upon it as it comes in.
 * 
 * @author Andreas Holstenson
 *
 * @param <T>
 */
public interface LogData<T>
	extends Iterable<LogEntry<T>>
{
	/**
	 * Get the first index committed in the log, or {@code 0} if no entries.
	 * 
	 * @return
	 */
	long first();
	
	/**
	 * Get the last index committed in the log, or {@code 0} if no entries.
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
	LogEntry<T> get(long index);
	
	@Override
	default Iterator<LogEntry<T>> iterator()
	{
		long first = first();
		long last = last();
		
		return new Iterator<LogEntry<T>>()
		{
			private long current = first;
			
			@Override
			public boolean hasNext()
			{
				return current < last;
			}
			
			@Override
			public LogEntry<T> next()
			{
				LogEntry<T> entry = get(current);
				current++;
				return entry;
			}
		};
	}
	
	/**
	 * Load several log entries at the same time.
	 * 
	 * @param ids
	 * @return
	 * @throws IOException
	 */
	default List<LogEntry<T>> get(long[] ids)
		throws IOException
	{
		List<LogEntry<T>> result = Lists.newArrayListWithCapacity(ids.length);
		for(long id : ids)
		{
			result.add(get(id));
		}
		return result;
	}
}
