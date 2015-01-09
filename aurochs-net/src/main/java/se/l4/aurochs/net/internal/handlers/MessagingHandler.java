package se.l4.aurochs.net.internal.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.Executor;

import se.l4.aurochs.core.io.ByteMessage;
import se.l4.aurochs.net.internal.TransportSession;

/**
 * Handler that receives messages and queues them on the specified executor.
 * 
 * @author Andreas Holstenson
 *
 */
public class MessagingHandler
	extends ChannelInboundHandlerAdapter
{
	private final TransportSession session;
	private final Executor executor;

	public MessagingHandler(Executor executor, TransportSession session)
	{
		this.executor = executor;
		this.session = session;
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
		throws Exception
	{
		executor.execute(new Runnable()
		{
			@Override
			public void run()
			{
				session.receive((ByteMessage) msg);
			}
		});
	}
}
