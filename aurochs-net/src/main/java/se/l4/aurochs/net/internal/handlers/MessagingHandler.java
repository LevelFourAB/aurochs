package se.l4.aurochs.net.internal.handlers;

import java.util.concurrent.Executor;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import se.l4.aurochs.net.internal.TransportSession;

/**
 * Handler that receives messages and queues them on the specified executor.
 * 
 * @author Andreas Holstenson
 *
 */
public class MessagingHandler
	extends SimpleChannelHandler
{
	private final TransportSession session;
	private final Executor executor;

	public MessagingHandler(Executor executor, TransportSession session)
	{
		this.executor = executor;
		this.session = session;
	}
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
		throws Exception
	{
		Object msg = e.getMessage();
		
		final se.l4.aurochs.core.channel.MessageEvent<Object> event = 
			new se.l4.aurochs.core.channel.MessageEvent<Object>(session, session, msg);
		
		executor.execute(new Runnable()
		{
			@Override
			public void run()
			{
				session.fireMessageReceived(event);
			}
		});
	}
}
