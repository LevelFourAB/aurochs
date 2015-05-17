package se.l4.aurochs.core.internal.log;

import java.util.concurrent.CompletableFuture;

import se.l4.aurochs.core.channel.ChannelCodec;
import se.l4.aurochs.core.log.LogData;
import se.l4.aurochs.core.log.LogEntry;
import se.l4.aurochs.core.log.StateLog;

public class TransformedStateLog<T, O>
	implements StateLog<T>
{
	private final StateLog<O> log;
	private final ChannelCodec<O, T> codec;
	private final TransformedLogData<T, O> data;

	public TransformedStateLog(StateLog<O> log, ChannelCodec<O, T> codec)
	{
		this.log = log;
		this.codec = codec;
		this.data = new TransformedLogData<>(log.data(), codec);
	}
	
	@Override
	public LogData<T> data()
	{
		return data;
	}

	@Override
	public CompletableFuture<LogEntry<T>> submit(T entry)
	{
		return log.submit(codec.toSource(entry))
			.thenApply(e -> new TransformedLogEntry<>(e, this.codec));
	}

	@Override
	public void close()
	{
		log.close();
	}

}
