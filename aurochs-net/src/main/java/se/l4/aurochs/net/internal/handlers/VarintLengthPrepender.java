package se.l4.aurochs.net.internal.handlers;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

@Sharable
public class VarintLengthPrepender
	extends OneToOneEncoder
{

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg)
		throws Exception
	{
		if(! (msg instanceof ChannelBuffer))
		{
			return null;
		}
		
		ChannelBuffer buffer = (ChannelBuffer) msg;
		int length = buffer.readableBytes();
		ChannelBuffer lengthBuffer = ChannelBuffers.dynamicBuffer(5);
		while(true)
		{
			if((length & ~0x7F) == 0)
			{
				lengthBuffer.writeByte(length);
				break;
			}
			else
			{
				lengthBuffer.writeByte((length & 0x7f) | 0x80);
				length >>>= 7;
			}
		}
		
		return ChannelBuffers.wrappedBuffer(lengthBuffer, buffer);
	}

}
