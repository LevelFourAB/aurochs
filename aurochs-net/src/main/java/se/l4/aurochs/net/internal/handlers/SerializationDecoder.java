package se.l4.aurochs.net.internal.handlers;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;

import se.l4.aurochs.net.internal.StreamFactory;
import se.l4.aurochs.serialization.format.StreamingInput;
import se.l4.aurochs.serialization.standard.CompactDynamicSerializer;

public class SerializationDecoder
	extends OneToOneDecoder
{
	private final CompactDynamicSerializer serializer;
	private final StreamFactory factory;

	public SerializationDecoder(CompactDynamicSerializer serializer, StreamFactory factory)
	{
		this.serializer = serializer;
		this.factory = factory;
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg)
		throws Exception
	{
		if(! (msg instanceof ChannelBuffer))
		{
			return msg;
		}
		
		StreamingInput in = factory.createInput((ChannelBuffer) msg);
		return serializer.read(in);
	}

}
