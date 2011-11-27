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
