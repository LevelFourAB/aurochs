package se.l4.aurochs.statelog.internal;

import se.l4.aurochs.channels.ChannelCodec;
import se.l4.aurochs.statelog.LogData;
import se.l4.aurochs.statelog.LogEntry;

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
}
