package se.l4.aurochs.core.internal.log;

import java.io.IOException;

import se.l4.aurochs.core.channel.ChannelCodec;
import se.l4.aurochs.core.io.IoConsumer;
import se.l4.aurochs.core.log.LogEntry;
import se.l4.aurochs.core.log.StateLog;
import se.l4.aurochs.core.log.StateLogBuilder;
import se.l4.aurochs.core.log.TransformedStateLogBuilder;

/**
 * Implementation of {@link TransformedStateLogBuilder≈.
 * 
 * @author Andreas Holstenson
 *
 * @param <T>
 * @param <O>
 */
public class TransformedStateLogBuilderImpl<T, O>
	implements TransformedStateLogBuilder<T>
{
	private final StateLogBuilder<O> builder;
	private final ChannelCodec<O, T> codec;

	public TransformedStateLogBuilderImpl(StateLogBuilder<O> builder, ChannelCodec<O, T> codec)
	{
		this.builder = builder;
		this.codec = codec;
	}
	
	@Override
	public <C> TransformedStateLogBuilder<C> transform(ChannelCodec<T, C> codec)
	{
		return new TransformedStateLogBuilderImpl<>(builder, this.codec.then(codec));
	}

	@Override
	public TransformedStateLogBuilder<T> withApplier(IoConsumer<LogEntry<T>> applier)
	{
		builder.withApplier(new TransformingConsumer<>(codec, applier));
		return this;
	}

	@Override
	public TransformedStateLogBuilder<T> withVolatileApplier(IoConsumer<LogEntry<T>> applier)
	{
		builder.withVolatileApplier(new TransformingConsumer<>(codec, applier));
		return this;
	}

	@Override
	public StateLog<T> build()
	{
		return new TransformedStateLog<>(builder.build(), codec);
	}
	
	private static class TransformingConsumer<T, O>
		implements IoConsumer<LogEntry<O>>
	{
		private final ChannelCodec<O, T> codec;
		private final IoConsumer<LogEntry<T>> consumer;

		public TransformingConsumer(ChannelCodec<O, T> codec, IoConsumer<LogEntry<T>> consumer)
		{
			this.codec = codec;
			this.consumer = consumer;
		}
		
		@Override
		public void accept(LogEntry<O> item)
			throws IOException
		{
			consumer.accept(new TransformedLogEntry<>(item, codec));
		}
	}
}
