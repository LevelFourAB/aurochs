package se.l4.aurochs.net.internal.handlers;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import se.l4.commons.io.Bytes;
import se.l4.commons.io.DefaultByteMessage;

public class ByteMessageDecoder
	extends ByteToMessageDecoder
{
	public ByteMessageDecoder()
	{
	}
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
		throws Exception
	{
		// First read the tag
		int shift = 0;
		long tag = 0;
		while(shift < 64)
		{
			if(! in.isReadable()) return;
			
			byte b = in.readByte();
			tag |= (long) (b & 0x7F) << shift;
			if((b & 0x80) == 0)
			{
				break;
			}
			
			shift += 7;
		}
		
		int bytesToRead = in.readableBytes();
		byte[] buf = new byte[bytesToRead];
		in.readBytes(buf);
	
		out.add(new DefaultByteMessage(tag, Bytes.create(buf)));
	}

}
