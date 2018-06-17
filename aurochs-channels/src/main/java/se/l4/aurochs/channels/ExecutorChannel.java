package se.l4.aurochs.channels;

import java.util.concurrent.Executor;

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
		
		channel.addMessageListener(this::handleMessageReceived);
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
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "{channel=" + channel + ", executor=" + executor + "}";
	}
}
