package se.l4.aurochs.transport.internal.handlers;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import se.l4.aurochs.transport.internal.TransportSession;

public class MessagingHandler
	extends SimpleChannelHandler
{
	private final TransportSession session;

	public MessagingHandler(TransportSession session)
	{
		this.session = session;
	}
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
		throws Exception
	{
		Object msg = e.getMessage();
		session.fireMessageReceived(new se.l4.aurochs.core.channel.MessageEvent<Object>(session, msg));
	}
}
