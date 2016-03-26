package se.l4.aurochs.net.internal.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import se.l4.commons.io.ByteMessage;

public class ByteMessageEncoder
	extends MessageToByteEncoder<ByteMessage>
{
	public ByteMessageEncoder()
	{
	}
	
	@Override
	protected void encode(ChannelHandlerContext ctx, ByteMessage msg, ByteBuf out)
		throws Exception
	{
		ByteMessage bm = msg;
		long tag = bm.getTag();
		while(true)
		{
			if((tag & ~0x7FL) == 0)
			{
				out.writeByte((int) tag);
				break;
			}
			else
			{
				out.writeByte(((int) tag & 0x7f) | 0x80);
				tag >>>= 7;
			}
		}
		
		try
		{
			bm.getData().asChunks(out::writeBytes);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw e;
		}
	}

}
