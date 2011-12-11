package se.l4.aurochs.net.internal.handlers;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

import se.l4.aurochs.net.internal.StreamFactory;
import se.l4.aurochs.serialization.format.StreamingOutput;
import se.l4.aurochs.serialization.standard.CompactDynamicSerializer;

public class SerializationEncoder
	extends OneToOneEncoder
{
	private final CompactDynamicSerializer serializer;
	private final StreamFactory factory;

	public SerializationEncoder(CompactDynamicSerializer serializer, StreamFactory factory)
	{
		this.serializer = serializer;
		this.factory = factory;
	}

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel,
			Object msg)
		throws Exception
	{
		ChannelBuffer buffer = ChannelBuffers.dynamicBuffer(4096);
		
		StreamingOutput out = factory.createOutput(buffer);
		serializer.write(msg, null, out);
		
		return buffer;
	}

}
