package se.l4.aurochs.transport.internal.handlers;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.group.ChannelGroup;

/**
 * Handler for server side functions.
 * 
 * @author Andreas Holstenson
 *
 */
public class ServerHandler
	extends SimpleChannelHandler
{
	private final ChannelGroup group;

	public ServerHandler(ChannelGroup group)
	{
		this.group = group;
	}
	
	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
		throws Exception
	{
		super.channelOpen(ctx, e);
		
		group.add(e.getChannel());
	}
}
