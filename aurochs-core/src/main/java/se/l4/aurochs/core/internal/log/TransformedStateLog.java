package se.l4.aurochs.core.internal.log;

import java.util.concurrent.CompletableFuture;

import se.l4.aurochs.core.channel.ChannelCodec;
import se.l4.aurochs.core.log.StateLog;

public class TransformedStateLog<T, O>
	implements StateLog<T>
{
	private final StateLog<O> log;
	private final ChannelCodec<O, T> codec;

	public TransformedStateLog(StateLog<O> log, ChannelCodec<O, T> codec)
	{
		this.log = log;
		this.codec = codec;
	}

	@Override
	public CompletableFuture<Void> submit(T entry)
	{
		return log.submit(codec.toSource(entry));
	}

	@Override
	public void close()
	{
		log.close();
	}

}
