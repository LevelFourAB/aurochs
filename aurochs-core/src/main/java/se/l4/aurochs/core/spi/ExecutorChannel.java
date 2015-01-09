package se.l4.aurochs.core.spi;

import java.util.concurrent.Executor;

import se.l4.aurochs.core.channel.Channel;
import se.l4.aurochs.core.channel.ChannelListener;
import se.l4.aurochs.core.channel.MessageEvent;

/**
 * Channel that executes events on a {@link Executor}.
 * 
 * @author Andreas Holstenson
 *
 * @param <T>
 */
public class ExecutorChannel<T>
	extends AbstractChannel<T>
{
	private final Channel<T> channel;
	private final Executor executor;

	public ExecutorChannel(Channel<T> channel, Executor executor)
	{
		this.channel = channel;
		this.executor = executor;
		
		channel.addListener(new ChannelListener<T>()
		{
			@Override
			public void messageReceived(MessageEvent<T> event)
			{
				handleMessageReceived(event);
			}
		});
	}

	protected void handleMessageReceived(final MessageEvent<T> event)
	{
		executor.execute(new Runnable()
		{
			@Override
			public void run()
			{
				fireMessageReceived(event);
			}
		});
	}

	@Override
	public void send(T message)
	{
		channel.send(message);
	}
	
	@Override
	public void close()
	{
		channel.close();
	}
}
