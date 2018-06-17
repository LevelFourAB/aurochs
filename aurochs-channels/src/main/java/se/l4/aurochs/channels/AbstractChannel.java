package se.l4.aurochs.channels;

import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Abstract session implementation.
 * 
 * @author Andreas Holstenson
 *
 */
public abstract class AbstractChannel<T>
	implements Channel<T>
{
	private static final MessageListener[] EMPTY = new MessageListener[0];
	
	private final Lock listenersLock;
	private volatile MessageListener[] listeners;

	public AbstractChannel()
	{
		listenersLock = new ReentrantLock();
		listeners = EMPTY;
	}

	@Override
	public void addMessageListener(MessageListener<T> listener)
	{
		listenersLock.lock();
		try
		{
			MessageListener[] listeners = this.listeners;
			
			for(int i=0, n=listeners.length; i<n; i++)
			{
				if(listeners[i] == listener)
				{
					// Already registered
					return;
				}
			}
			
			MessageListener[] result = new MessageListener[listeners.length + 1];
			System.arraycopy(listeners, 0, result, 0, listeners.length);
			result[listeners.length] = listener;
			
			this.listeners = result;
		}
		finally
		{
			listenersLock.unlock();
		}
	}
	
	@Override
	public void removeMessageListener(MessageListener<T> listener)
	{
		listenersLock.lock();
		try
		{
			MessageListener[] listeners = this.listeners;
			int index = -1;
			for(int i=0, n=listeners.length; i<n; i++)
			{
				if(listeners[i] == listener)
				{
					index = i;
					break;
				}
			}
			
			if(index == -1)
			{
				// Nothing to do, no such listener
				return;
			}
			
			int length = listeners.length;
			MessageListener[] result = new MessageListener[length - 1];
			System.arraycopy(listeners, 0, result, 0, index);
			
			if(index < length - 1)
			{
				System.arraycopy(listeners, index + 1, result, index, length - index - 1);
			}
			
			this.listeners = result;
		}
		finally
		{
			listenersLock.unlock();
		}
	}
	
	protected void fireMessageReceived(MessageEvent<? extends T> event)
	{
		MessageListener[] listeners = this.listeners;
		for(MessageListener listener : listeners)
		{
			listener.messageReceived(event);
		}
	}
	
	@Override
	public <O> Channel<O> filter(Class<O> type)
	{
		return new FilteredChannel<>(this, o -> type.isAssignableFrom(o.getClass()));
	}
	
	@Override
	public Channel<T> filter(Predicate<T> predicate)
	{
		return new FilteredChannel<>(this, predicate);
	}
	
	@Override
	public Channel<T> on(Executor executor)
	{
		return new ExecutorChannel<T>(this, executor);
	}
	
	@Override
	public <N> Channel<N> transform(Function<N, T> fromSource, Function<T, N> toSource)
	{
		return transform(ChannelCodec.create(toSource, fromSource));
	}
	
	@Override
	public <N> Channel<N> transform(ChannelCodec<T, N> codec)
	{
		return new CodecChannel<>(this, codec);
	}
}
