package se.l4.aurochs.net.internal.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import se.l4.aurochs.core.io.ByteMessage;

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
		int tag = bm.getTag();
		while(true)
		{
			if((tag & ~0x7F) == 0)
			{
				out.writeByte(tag);
				break;
			}
			else
			{
				out.writeByte((tag & 0x7f) | 0x80);
				tag >>>= 7;
			}
		}
		
		bm.getData().asChunks(out::writeBytes);
	}

}