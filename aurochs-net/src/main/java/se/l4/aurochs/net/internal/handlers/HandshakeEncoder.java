package se.l4.aurochs.net.internal.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import se.l4.aurochs.net.internal.handshake.Authenticate;
import se.l4.aurochs.net.internal.handshake.BeginSession;
import se.l4.aurochs.net.internal.handshake.Capabilities;
import se.l4.aurochs.net.internal.handshake.Ok;
import se.l4.aurochs.net.internal.handshake.Reject;
import se.l4.aurochs.net.internal.handshake.SelectCapabilities;
import se.l4.aurochs.net.internal.handshake.SessionStatus;

import com.google.common.base.Charsets;

public class HandshakeEncoder
	extends MessageToByteEncoder<Object>
{
	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out)
		throws Exception
	{
		if(msg instanceof Capabilities || msg instanceof SelectCapabilities
			|| msg instanceof Authenticate || msg instanceof Ok
			|| msg instanceof Reject || msg instanceof BeginSession
			|| msg instanceof SessionStatus)
		{
			out.writeBytes(msg.toString().getBytes(Charsets.UTF_8));
			out.writeByte('\n');
		}
	}
}
