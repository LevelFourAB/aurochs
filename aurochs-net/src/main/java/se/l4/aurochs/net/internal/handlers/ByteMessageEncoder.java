package se.l4.aurochs.net.internal.handlers;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

import se.l4.aurochs.core.io.ByteMessage;

public class ByteMessageEncoder
	extends OneToOneEncoder
{
	public ByteMessageEncoder()
	{
	}

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg)
		throws Exception
	{
		ChannelBuffer buffer = ChannelBuffers.dynamicBuffer(4096);
		
		ByteMessage bm = (ByteMessage) msg;
		int tag = bm.getTag();
		while(true)
		{
			if((tag & ~0x7F) == 0)
			{
				buffer.writeByte(tag);
				break;
			}
			else
			{
				buffer.writeByte((tag & 0x7f) | 0x80);
				tag >>>= 7;
			}
		}
		
		bm.getData().asChunks(buffer::writeBytes);
		
		return buffer;
	}

}
