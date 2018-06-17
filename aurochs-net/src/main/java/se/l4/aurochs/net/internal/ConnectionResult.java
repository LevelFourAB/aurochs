package se.l4.aurochs.net.internal;

import se.l4.aurochs.sessions.Session;

/**
 * Result for a connection attempt.
 * 
 * @author Andreas Holstenson
 *
 */
public class ConnectionResult
{
	private final Session session;
	private final Throwable exception;
	
	public ConnectionResult(Session session)
	{
		this.session = session;
		this.exception = null;
	}
	
	public ConnectionResult(Throwable exception)
	{
		this.exception = exception;
		this.session = null;
	}
	
	public Session getSession()
	{
		return session;
	}
	
	public Throwable getException()
	{
		return exception;
	}
	
	public boolean isError()
	{
		return exception != null;
	}
}
