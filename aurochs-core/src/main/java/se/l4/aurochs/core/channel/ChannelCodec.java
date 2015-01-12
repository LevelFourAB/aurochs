package se.l4.aurochs.core.channel;

import java.util.function.Function;

/**
 * Codec for {@link Channel channels}, combines transformation with filtering
 * to create a new type of channel.
 * 
 * @author Andreas Holstenson
 *
 * @param <From>
 * @param <To>
 */
public interface ChannelCodec<From, To>
{
	/**
	 * Get the filter to use for this codec.
	 * 
	 * @return
	 */
	boolean accepts(From in);
	
	/**
	 * Convert from the source to the target.
	 * 
	 * @param object
	 * @return
	 */
	To fromSource(From object);
	
	/**
	 * Convert from the target to the source.
	 * 
	 * @param object
	 * @return
	 */
	From toSource(To object);
	
	/**
	 * Create a codec that matches all messages and transforms with the two
	 * specified functions.
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	static <O, T> ChannelCodec<O, T> create(Function<O, T> fromSource, Function<T, O> toSource)
	{
		return new ChannelCodec<O, T>()
		{
			@Override
			public boolean accepts(O in)
			{
				return true;
			}
			
			@Override
			public T fromSource(O object)
			{
				return fromSource.apply(object);
			}
			
			@Override
			public O toSource(T object)
			{
				return toSource.apply(object);
			}
		};
	}
}
