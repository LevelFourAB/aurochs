package se.l4.aurochs.statelog;

import java.util.function.Function;

import se.l4.aurochs.channels.ChannelCodec;
import se.l4.commons.io.IoConsumer;

public interface StateLogBuilder<T>
{
	/**
	 * Transform the data the state log handles.
	 * 
	 * @param codec
	 * @return
	 */
	default <O> StateLogBuilder<O> transform(ChannelCodec<T, O> codec)
	{
		return TransformedStateLogBuilder.create(this, codec);
	}
	
	/**
	 * Transform the data the state log handles using two functions.
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	default <O> StateLogBuilder<O> transform(Function<T, O> from, Function<O, T> to)
	{
		return transform(ChannelCodec.create(from, to));
	}
	
	/**
	 * Set the consumer that is used to commit entries in the log. The applier
	 * will be called at most once for every successful commit. If the
	 * log is closed and then opened again the applier will not be called for
	 * previous log entries.
	 * 
	 * <p>
	 * If your service has a in-memory representation of data use
	 * {@link #withVolatileApplier(IoConsumer)} instead.
	 * 
	 * @param applier
	 * @return
	 */
	StateLogBuilder<T> withApplier(IoConsumer<LogEntry<T>> applier);
	
	/**
	 * Set the consumer that is used to commit entries in the log, optionally specifying
	 * that it should be volatile. This method will call either {@link #withApplier(IoConsumer)}
	 * or {@link #withVolatileApplier(IoConsumer)}.
	 * 
	 * @param applier
	 * @param isVolatile
	 * @return
	 */
	default StateLogBuilder<T> withApplier(IoConsumer<LogEntry<T>> applier, boolean isVolatile)
	{
		if(isVolatile)
		{
			return withVolatileApplier(applier);
		}
		else
		{
			return withApplier(applier);
		}
	}
	
	/**
	 * Set the consumer that is used to commit entries in the log. This is
	 * similar to {@link #withApplier(IoConsumer)} with the change that this
	 * applier will be called for all previous log entries when the log is
	 * opened.
	 *  
	 * @param applier
	 * @return
	 */
	StateLogBuilder<T> withVolatileApplier(IoConsumer<LogEntry<T>> applier);
	
	StateLog<T> build();
}
