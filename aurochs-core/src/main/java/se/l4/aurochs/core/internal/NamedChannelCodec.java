package se.l4.aurochs.core.internal;

import java.nio.charset.StandardCharsets;

import se.l4.aurochs.core.channel.ChannelCodec;
import se.l4.aurochs.core.io.ByteMessage;
import se.l4.aurochs.core.io.Bytes;
import se.l4.aurochs.core.io.DefaultByteMessage;

import com.google.common.hash.Hashing;

public class NamedChannelCodec
	implements ChannelCodec<ByteMessage, Bytes>
{
	private long id;

	public NamedChannelCodec(String name)
	{
		long hash = Integer.toUnsignedLong(Hashing.murmur3_32()
			.hashString(name, StandardCharsets.UTF_8)
			.asInt()
		);
		
		this.id = 1024 + hash;
	}

	@Override
	public boolean accepts(ByteMessage in)
	{
		return in.getTag() == id;
	}

	@Override
	public Bytes fromSource(ByteMessage object)
	{
		return object.getData();
	}

	@Override
	public ByteMessage toSource(Bytes object)
	{
		return new DefaultByteMessage(id, object);
	}

}
