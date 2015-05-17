package se.l4.aurochs.core.log;

import java.util.Iterator;

/**
 * Builder of a {@link StreamingStateLog}.
 * 
 * @author Andreas Holstenson
 *
 */
public interface StreamingStateLogBuilder<T>
	extends StateLogBuilder<Iterator<T>>
{
	/**
	 * Build the state log.
	 * 
	 * @return
	 */
	@Override
	StreamingStateLog<T> build();
}
