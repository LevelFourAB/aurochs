package se.l4.aurochs.net.internal.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;

import java.util.List;

import se.l4.aurochs.serialization.format.BinaryInput;

/**
 * Decoder that will read a variable integer (as used in {@link BinaryInput})
 * at the start of each message.
 * 
 * @author Andreas Holstenson
 *
 */
public class VarintFrameDecoder
	extends ByteToMessageDecoder
{

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
		throws Exception
	{
		in.markReaderIndex();
		
		int shift = 0;
		int length = 0;
		while(shift < 32)
		{
			if(! in.isReadable())
			{
				// Can't read the entire length
				in.resetReaderIndex();
				return;
			}
			
			final byte b = in.readByte();
			length |= (b & 0x7F) << shift;
			if((b & 0x80) == 0)
			{
				if(in.readableBytes() < length)
				{
					// Not enough bytes to read the entire message
					in.resetReaderIndex();
					return;
				}
				else
				{
					out.add(in.readBytes(length));
					return;
				}
			}
			
			shift += 7;
		}
		
		throw new CorruptedFrameException("Invalid integer");
	}

}
