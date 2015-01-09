package se.l4.aurochs.core.internal;

import se.l4.aurochs.core.SystemSession;
import se.l4.aurochs.core.channel.MessageEvent;
import se.l4.aurochs.core.spi.AbstractSession;

import com.google.inject.Injector;

/**
 * Implementation of {@link SystemSession}.
 * 
 * @author Andreas Holstenson
 *
 */
public class SystemSessionImpl
	extends AbstractSession
	implements SystemSession
{
	public SystemSessionImpl(Injector injector)
	{
		super(injector);
	}

	@Override
	public Injector getInjector()
	{
		return injector;
	}
	
	@Override
	public void send(Object message)
	{
		fireMessageReceived(new MessageEvent<Object>(this, this, message));
	}
	
	@Override
	public void close()
	{
	}
}
