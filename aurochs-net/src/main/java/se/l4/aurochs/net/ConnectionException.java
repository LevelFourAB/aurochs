package se.l4.aurochs.net;

/**
 * Exception thrown when a connection can not be established.
 * 
 * @author Andreas Holstenson
 *
 */
public class ConnectionException
	extends Exception
{
	public ConnectionException()
	{
		super();
	}

	public ConnectionException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ConnectionException(String message)
	{
		super(message);
	}

	public ConnectionException(Throwable cause)
	{
		super(cause);
	}
}
