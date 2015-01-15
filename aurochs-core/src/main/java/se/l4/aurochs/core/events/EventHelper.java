package se.l4.aurochs.core.events;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class EventHelper<L>
{
	private static final Object[] EMPTY = new Object[0];
	
	private final Lock listenersLock;
	private volatile Object[] listeners;

	public EventHelper()
	{
		listenersLock = new ReentrantLock();
		listeners = EMPTY;
	}

	public void addListener(L listener)
	{
		listenersLock.lock();
		try
		{
			Object[] listeners = this.listeners;
			
			Object[] result = new Object[listeners.length + 1];
			System.arraycopy(listeners, 0, result, 0, listeners.length);
			result[listeners.length] = listener;
			
			this.listeners = result;
		}
		finally
		{
			listenersLock.unlock();
		}
	}
	
	public void removeListener(L listener)
	{
		listenersLock.lock();
		try
		{
			Object[] listeners = this.listeners;
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
			Object[] result = new Object[length - 1];
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
	
	public void forEach(Consumer<L> consumer)
	{
		Object[] listeners = this.listeners;
		for(Object listener : listeners)
		{
			consumer.accept((L) listener);
		}
	}
	
	public EventHandle listen(L listener)
	{
		addListener(listener);
		
		return () -> removeListener(listener);
	}
}
