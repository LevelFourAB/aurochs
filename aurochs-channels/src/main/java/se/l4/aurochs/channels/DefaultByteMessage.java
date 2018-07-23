package se.l4.aurochs.channels;

import se.l4.commons.io.Bytes;

/**
 * Default implementation of {@link ByteMessage}.
 */
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

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "{tag=" + tag + ", data=" + data + "}";
	}
}