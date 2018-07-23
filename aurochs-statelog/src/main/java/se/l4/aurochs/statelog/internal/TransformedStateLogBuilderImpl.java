package se.l4.aurochs.statelog.internal;

import java.io.IOException;

import se.l4.aurochs.channels.ChannelCodec;
import se.l4.aurochs.statelog.LogEntry;
import se.l4.aurochs.statelog.StateLog;
import se.l4.aurochs.statelog.StateLogBuilder;
import se.l4.aurochs.statelog.TransformedStateLogBuilder;
import se.l4.commons.io.IOConsumer;

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
	public TransformedStateLogBuilder<T> withApplier(IOConsumer<LogEntry<T>> applier)
	{
		builder.withApplier(new TransformingConsumer<>(codec, applier));
		return this;
	}

	@Override
	public TransformedStateLogBuilder<T> withVolatileApplier(IOConsumer<LogEntry<T>> applier)
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
		implements IOConsumer<LogEntry<O>>
	{
		private final ChannelCodec<O, T> codec;
		private final IOConsumer<LogEntry<T>> consumer;

		public TransformingConsumer(ChannelCodec<O, T> codec, IOConsumer<LogEntry<T>> consumer)
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
