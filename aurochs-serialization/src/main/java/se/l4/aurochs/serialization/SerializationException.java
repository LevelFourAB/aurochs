package se.l4.aurochs.serialization;

/**
 * Exception that is related to errors with serialization.
 * 
 * @author Andreas Holstenson
 *
 */
public class SerializationException
	extends RuntimeException
{
	private static final long serialVersionUID = -7546859789152950837L;

	public SerializationException()
	{
		super();
	}

	public SerializationException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public SerializationException(String message)
	{
		super(message);
	}

	public SerializationException(Throwable cause)
	{
		super(cause);
	}
	
}
