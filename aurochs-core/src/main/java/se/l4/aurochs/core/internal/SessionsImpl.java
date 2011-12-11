package se.l4.aurochs.core.internal;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import se.l4.aurochs.core.Session;
import se.l4.aurochs.core.spi.Sessions;

import com.google.inject.Singleton;

/**
 * Implementation of {@link Sessions}.
 * 
 * @author Andreas Holstenson
 *
 */
@Singleton
public class SessionsImpl
	implements Sessions
{
	private final ThreadLocal<Session> active;
	
	private final Lock listenersLock;
	private volatile Listener[] listeners;
	
	public SessionsImpl()
	{
		active = new ThreadLocal<Session>();
		listenersLock = new ReentrantLock();
		listeners = new Listener[0];
	}
	
	@Override
	public void addListener(Listener listener)
	{
		listenersLock.lock();
		try
		{
			Listener[] listeners = this.listeners;
			
			Listener[] result = new Listener[listeners.length + 1];
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
	public void removeListener(Listener listener)
	{
		listenersLock.lock();
		try
		{
			Listener[] listeners = this.listeners;
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
			Listener[] result = new Listener[length - 1];
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

	@Override
	public void create(Session session)
	{
		Listener[] listeners = this.listeners;
		for(Listener l : listeners)
		{
			l.sessionCreated(session);
		}
	}

	@Override
	public void destroy(Session session)
	{
		Listener[] listeners = this.listeners;
		for(Listener l : listeners)
		{
			l.sessionDestroyed(session);
		}
	}

	@Override
	public Session activate(Session session)
	{
		Session current = active.get();
		active.set(session);
		return current;
	}
	
	@Override
	public Session getActive()
	{
		return active.get();
	}

}
