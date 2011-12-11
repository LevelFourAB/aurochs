package se.l4.aurochs.net.internal.handlers;

import javax.xml.bind.DatatypeConverter;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;

import se.l4.aurochs.net.internal.handshake.Authenticate;
import se.l4.aurochs.net.internal.handshake.BeginSession;
import se.l4.aurochs.net.internal.handshake.Capabilities;
import se.l4.aurochs.net.internal.handshake.Ok;
import se.l4.aurochs.net.internal.handshake.Reject;
import se.l4.aurochs.net.internal.handshake.SelectCapabilities;
import se.l4.aurochs.net.internal.handshake.SessionStatus;

import com.google.common.base.Charsets;

@Sharable
public class HandshakeDecoder
	extends OneToOneDecoder
{
	private static final String BEGIN = "BEGIN";
	private static final String CAPS = "CAPS";
	private static final String CAPS_SELECT = "SELECT";
	private static final String AUTH = "AUTH";
	private static final String OK = "OK";
	private static final String REJECT = "REJECT";
	private static final String SESSION = "SESSION";

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg)
		throws Exception
	{
		if(! (msg instanceof ChannelBuffer))
		{
			return msg;
		}
		
		ChannelBuffer buffer = (ChannelBuffer) msg;
		String chars = buffer.toString(Charsets.UTF_8);
		if(chars.startsWith(CAPS))
		{
			String rest = chars.substring(CAPS.length()).trim();
			return new Capabilities(rest.split(" "));
		}
		else if(chars.startsWith(CAPS_SELECT))
		{
			String rest = chars.substring(CAPS_SELECT.length()).trim();
			return new SelectCapabilities(rest.split(" "));
		}
		else if(chars.startsWith(OK))
		{
			return new Ok();
		}
		else if(chars.startsWith(AUTH))
		{
			String rest = chars.substring(AUTH.length()).trim();
			if(rest.length() > 0)
			{
				int idx = rest.indexOf(' ');
				if(idx > 0)
				{
					String method = rest.substring(0, idx);
					String base64 = rest.substring(idx+1);
					
					return new Authenticate(method, DatatypeConverter.parseBase64Binary(base64));
				}
				else
				{
					return new Authenticate(rest, new byte[0]);
				}
			}
			else
			{
				return new Authenticate();
			}
		}
		else if(chars.startsWith(REJECT))
		{
			String rest = chars.substring(REJECT.length()).trim();
			return new Reject(rest);
		}
		else if(chars.startsWith(BEGIN))
		{
			String id = chars.substring(BEGIN.length()).trim();
			return new BeginSession(id.length() == 0 ? null : id);
		}
		else if(chars.startsWith(SESSION))
		{
			String id = chars.substring(SESSION.length()).trim();
			return new SessionStatus(id);
		}
		
		return new Reject("Unknown message: " + chars);
	}

}
