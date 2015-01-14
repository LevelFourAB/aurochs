package se.l4.aurochs.core.io;

public class DefaultByteMessage
	implements ByteMessage
{
	private final long tag;
	private final Bytes data;
	
	public DefaultByteMessage(long tag, Bytes data)
	{
		this.tag = tag;
		this.data = data;
	}
	
	@Override
	public long getTag()
	{
		return tag;
	}
	
	@Override
	public Bytes getData()
	{
		return data;
	}
}
