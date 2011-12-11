package se.l4.aurochs.transport.internal.handshake;

/**
 * Rejection of the specified command.
 * 
 * @author Andreas Holstenson
 *
 */
public class Reject
{
	private final String message;

	public Reject(String message)
	{
		this.message = message;
	}
	
	public String getMessage()
	{
		return message;
	}
	
	@Override
	public String toString()
	{
		return "REJECT " + message;
	}
}
