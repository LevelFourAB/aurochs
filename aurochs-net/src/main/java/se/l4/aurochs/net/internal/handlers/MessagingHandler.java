package se.l4.aurochs.net.internal.handlers;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import se.l4.commons.io.ByteMessage;
import se.l4.commons.io.Bytes;
import se.l4.commons.io.DefaultByteMessage;

/**
 * Handler that receives messages and queues them on the specified executor.
 * 
 * @author Andreas Holstenson
 *
 */
public class MessagingHandler
	extends ChannelDuplexHandler
{
	private final Executor executor;
	private final Consumer<ByteMessage> messageReceiver;

	public MessagingHandler(Executor executor, Consumer<ByteMessage> messageReceiver)
	{
		this.executor = executor;
		this.messageReceiver = messageReceiver;
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
		throws Exception
	{
		executor.execute(() -> messageReceiver.accept((ByteMessage) msg));
	}
	
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
		throws Exception
	{
		if(evt instanceof IdleStateEvent)
		{
			IdleStateEvent event = (IdleStateEvent) evt;
			
			if(event.state() == IdleState.READER_IDLE)
			{
				ctx.close();
			}
			else if(event.state() == IdleState.WRITER_IDLE)
			{
				ctx.writeAndFlush(new DefaultByteMessage(0, Bytes.empty()));
			}
		}
			
		super.userEventTriggered(ctx, evt);
	}
}
