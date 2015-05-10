package se.l4.aurochs.core.log;

import se.l4.aurochs.core.channel.ChannelCodec;
import se.l4.aurochs.core.internal.log.TransformedStateLogBuilderImpl;

/**
 * Version of {@link StateLogBuilder} for transforming data in the log.
 * 
 * @author Andreas Holstenson
 *
 * @param <T>
 */
public interface TransformedStateLogBuilder<T>
	extends StateLogBuilder<T>
{
	/**
	 * Create a new builder.
	 * 
	 * @param builder
	 * @param codec
	 * @return
	 */
	static <C, O> TransformedStateLogBuilder<O> create(StateLogBuilder<C> builder, ChannelCodec<C, O> codec)
	{
		return new TransformedStateLogBuilderImpl<>(builder, codec);
	}
}
