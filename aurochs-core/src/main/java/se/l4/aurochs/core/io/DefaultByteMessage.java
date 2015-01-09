package se.l4.aurochs.core.io;

public class DefaultByteMessage
	implements ByteMessage
{
	private final int tag;
	private final Bytes data;
	
	public DefaultByteMessage(int tag, Bytes data)
	{
		this.tag = tag;
		this.data = data;
	}
	
	@Override
	public int getTag()
	{
		return tag;
	}
	
	@Override
	public Bytes getData()
	{
		return data;
	}
}
