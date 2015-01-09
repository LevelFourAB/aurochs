package se.l4.aurochs.core.internal;

import se.l4.aurochs.core.SystemSession;
import se.l4.aurochs.core.channel.Channel;
import se.l4.aurochs.core.io.ByteMessage;
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
	public Channel<Object> getObjectChannel()
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Channel<ByteMessage> getRawChannel()
	{
		throw new UnsupportedOperationException();
	}
}
