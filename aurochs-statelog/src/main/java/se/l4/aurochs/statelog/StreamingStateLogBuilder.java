package se.l4.aurochs.statelog;

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
