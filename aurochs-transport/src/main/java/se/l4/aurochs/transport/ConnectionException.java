package se.l4.aurochs.transport;

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
