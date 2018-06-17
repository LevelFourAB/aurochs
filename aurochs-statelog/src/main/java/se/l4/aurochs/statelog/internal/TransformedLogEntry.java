package se.l4.aurochs.statelog.internal;

import java.io.IOException;

import se.l4.aurochs.channels.ChannelCodec;
import se.l4.aurochs.statelog.LogEntry;

public class TransformedLogEntry<T, O>
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