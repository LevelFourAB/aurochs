package se.l4.aurochs.net.internal.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;

/**
 * Handler for server side functions.
 * 
 * @author Andreas Holstenson
 *
 */
public class ServerHandler
	extends ChannelInboundHandlerAdapter
{
	private final ChannelGroup group;

	public ServerHandler(ChannelGroup group)
	{
		this.group = group;
	}
	
	@Override
	public void channelRegistered(ChannelHandlerContext ctx)
		throws Exception
	{
		super.channelRegistered(ctx);
		
		group.add(ctx.channel());
	}
	
	@Override
	public void channelUnregistered(ChannelHandlerContext ctx)
		throws Exception
	{
		super.channelUnregistered(ctx);
		
		group.remove(ctx.channel());
	}
}
