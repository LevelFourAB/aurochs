package se.l4.aurochs.net.internal.handlers;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;

import se.l4.aurochs.core.io.Bytes;
import se.l4.aurochs.core.io.DefaultByteMessage;

public class ByteMessageDecoder
	extends OneToOneDecoder
{
	public ByteMessageDecoder()
	{
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg)
		throws Exception
	{
		if(! (msg instanceof ChannelBuffer))
		{
			return msg;
		}
		
		ChannelBuffer buffer = (ChannelBuffer) msg;
		
		// First read the tag
		int shift = 0;
		int tag = 0;
		while(shift < 32)
		{
			byte b = buffer.readByte();
			tag |= (b & 0x7F) << shift;
			if((b & 0x80) == 0)
			{
				break;
			}
			
			shift += 7;
		}
		
		int bytesToRead = buffer.readableBytes();
		byte[] buf = new byte[bytesToRead];
		buffer.readBytes(buf);
	
		return new DefaultByteMessage(tag, Bytes.create(buf));
	}

}
