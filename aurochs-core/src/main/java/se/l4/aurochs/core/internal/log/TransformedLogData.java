package se.l4.aurochs.core.internal.log;

import java.io.IOException;

import se.l4.aurochs.core.channel.ChannelCodec;
import se.l4.aurochs.core.log.LogData;
import se.l4.aurochs.core.log.LogEntry;

public class TransformedLogData<T, O>
	implements LogData<T>
{
	private final LogData<O> data;
	private final ChannelCodec<O, T> codec;

	public TransformedLogData(LogData<O> data, ChannelCodec<O, T> codec)
	{
		this.data = data;
		this.codec = codec;
	}
	
	@Override
	public long first()
	{
		return data.first();
	}
	
	@Override
	public long last()
	{
		return data.last();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public LogEntry<T> get(long index)
	{
		LogEntry<O> entry = data.get(index);
		if(entry.type() != LogEntry.Type.DATA)
		{
			// Skip conversion if this is not of type data
			return (LogEntry) entry;
		}
		
		return new TransformedLogEntry(entry, codec);
	}
	
	private static class TransformedLogEntry<T, O>
		implements LogEntry<T>
	{
		private final LogEntry<O> other;
		private final ChannelCodec<O, T> codec;

		public TransformedLogEntry(LogEntry<O> other, ChannelCodec<O, T> codec)
		{
			this.other = other;
			this.codec = codec;
		}
		
		@Override
		public long id()
		{
			return other.id();
		}
		
		@Override
		public Type type()
		{
			return other.type();
		}
		
		@Override
		public T data()
			throws IOException
		{
			O data = other.data();
			return codec.fromSource(data);
		}
	}
}
