package se.l4.aurochs.net.internal.handlers;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

import se.l4.aurochs.net.internal.handshake.Authenticate;
import se.l4.aurochs.net.internal.handshake.BeginSession;
import se.l4.aurochs.net.internal.handshake.Capabilities;
import se.l4.aurochs.net.internal.handshake.Ok;
import se.l4.aurochs.net.internal.handshake.Reject;
import se.l4.aurochs.net.internal.handshake.SelectCapabilities;
import se.l4.aurochs.net.internal.handshake.SessionStatus;

import com.google.common.base.Charsets;

@Sharable
public class HandshakeEncoder
	extends OneToOneEncoder
{
	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg)
		throws Exception
	{
		if(msg instanceof Capabilities || msg instanceof SelectCapabilities
			|| msg instanceof Authenticate || msg instanceof Ok
			|| msg instanceof Reject || msg instanceof BeginSession
			|| msg instanceof SessionStatus)
		{
			ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
			buffer.writeBytes(msg.toString().getBytes(Charsets.UTF_8));
			buffer.writeByte('\n');
			return buffer;
		}
		
		return msg;
	}
}
