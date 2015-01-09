package se.l4.aurochs.net.internal.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import javax.xml.bind.DatatypeConverter;

import se.l4.aurochs.net.internal.handshake.Authenticate;
import se.l4.aurochs.net.internal.handshake.BeginSession;
import se.l4.aurochs.net.internal.handshake.Capabilities;
import se.l4.aurochs.net.internal.handshake.Ok;
import se.l4.aurochs.net.internal.handshake.Reject;
import se.l4.aurochs.net.internal.handshake.SelectCapabilities;
import se.l4.aurochs.net.internal.handshake.SessionStatus;

import com.google.common.base.Charsets;

public class HandshakeDecoder
	extends ByteToMessageDecoder
{
	private static final String BEGIN = "BEGIN";
	private static final String CAPS = "CAPS";
	private static final String CAPS_SELECT = "SELECT";
	private static final String AUTH = "AUTH";
	private static final String OK = "OK";
	private static final String REJECT = "REJECT";
	private static final String SESSION = "SESSION";

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
		throws Exception
	{
		Object object = decode(in);
		if(object != null)
		{
			out.add(object);
		}
	}
	
	private int findLineEnding(ByteBuf buffer)
	{
		for(int i=buffer.readerIndex(), n=buffer.writerIndex(); i<n; i++)
		{
			if(buffer.getByte(i) == '\n') return i;
		}
		
		return -1;
	}
	
	protected Object decode(ByteBuf buffer)
		throws Exception
	{
		int eol = findLineEnding(buffer);
		if(eol == -1) return null;
		
		ByteBuf sliced = buffer.readSlice(eol - buffer.readerIndex());
		buffer.skipBytes(1); // Skip \n
		
		String chars = sliced.toString(Charsets.UTF_8);
		
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
