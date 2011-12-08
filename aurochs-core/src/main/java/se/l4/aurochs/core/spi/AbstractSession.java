package se.l4.aurochs.core.spi;

import se.l4.aurochs.core.Session;

import com.google.inject.Injector;

public abstract class AbstractSession
	extends AbstractChannel<Object>
	implements Session
{
	protected final Injector injector;
	
	public AbstractSession(Injector injector)
	{
		this.injector = injector;
	}
}
