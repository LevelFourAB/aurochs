package se.l4.aurochs.transport.internal.handlers;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.CorruptedFrameException;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

import se.l4.aurochs.serialization.format.BinaryInput;

/**
 * Decoder that will read a variable integer (as used in {@link BinaryInput})
 * at the start of each message.
 * 
 * @author Andreas Holstenson
 *
 */
@Sharable
public class VarintFrameDecoder
	extends FrameDecoder
{

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel,
			ChannelBuffer buffer)
		throws Exception
	{
		buffer.markReaderIndex();
		
		int shift = 0;
		int length = 0;
		while(shift < 32)
		{
			if(! buffer.readable())
			{
				// Can't read the entire length
				buffer.resetReaderIndex();
				return null;
			}
			
			final byte b = buffer.readByte();
			length |= (int) (b & 0x7F) << shift;
			if((b & 0x80) == 0)
			{
				if(buffer.readableBytes() < length)
				{
					// Not enough bytes to read the entire message
					buffer.resetReaderIndex();
					return null;
				}
				else
				{
					return buffer.readBytes(length);
				}
			}
			
			shift += 7;
		}
		
		throw new CorruptedFrameException("Invalid integer");
	}

}
