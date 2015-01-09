package se.l4.aurochs.net.internal.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class VarintLengthPrepender
	extends MessageToByteEncoder<ByteBuf>
{
	@Override
	protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out)
		throws Exception
	{
		System.out.println("v1 " + out.writerIndex());
		int l = msg.readableBytes();
		int length = msg.readableBytes();
		while(true)
		{
			if((length & ~0x7F) == 0)
			{
				out.writeByte(length);
				break;
			}
			else
			{
				out.writeByte((length & 0x7f) | 0x80);
				length >>>= 7;
			}
		}
		
		System.out.println("v2 " + out.writerIndex());
		
		out.writeBytes(msg, msg.readerIndex(), l);
		
		System.out.println("v3 " + out.writerIndex());
	}

}
