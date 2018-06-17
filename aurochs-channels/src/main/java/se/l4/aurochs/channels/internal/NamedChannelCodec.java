package se.l4.aurochs.channels.internal;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;

import se.l4.aurochs.channels.ChannelCodec;
import se.l4.commons.io.ByteMessage;
import se.l4.commons.io.Bytes;
import se.l4.commons.io.DefaultByteMessage;

public class NamedChannelCodec
	implements ChannelCodec<ByteMessage, Bytes>
{
	private static final Map<Long, String> CREATED_CODECS = Maps.newHashMap();
	
	private long id;

	public NamedChannelCodec(String name)
	{
		long hash = Integer.toUnsignedLong(Hashing.murmur3_32()
			.hashString(name, StandardCharsets.UTF_8)
			.asInt()
		);
		
		this.id = 1024 + hash;
		
		String old = CREATED_CODECS.get(id);
		if(old == null)
		{
			CREATED_CODECS.put(id, name);
		}
		else if(! name.equals(old))
		{
			throw new Error("Hash conflict detected between `" + name + "` and already registered channel `" + old + "`");
		}
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
