package se.l4.aurochs.core.spi;

import se.l4.aurochs.core.Session;

import com.google.inject.Injector;
import com.google.inject.Key;

/**
 * Abstract implementation of {@link Session}.
 * 
 * @author Andreas Holstenson
 *
 */
public abstract class AbstractSession
	implements Session
{
	protected final Injector injector;
	protected final Sessions sessions;
	
	public AbstractSession(Injector injector)
	{
		this.injector = injector;
		
		sessions = injector.getInstance(Sessions.class);
	}
	
	@Override
	public <T> T get(Class<T> instance)
	{
		Session old = sessions.activate(this);
		try
		{
			return injector.getInstance(instance);
		}
		finally
		{
			sessions.activate(old);
		}
	}
	
	@Override
	public <T> T get(Key<T> key)
	{
		Session old = sessions.activate(this);
		try
		{
			return injector.getInstance(key);
		}
		finally
		{
			sessions.activate(old);
		}
	}
}
