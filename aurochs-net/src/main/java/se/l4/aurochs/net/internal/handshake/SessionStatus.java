package se.l4.aurochs.net.internal.handshake;

/**
 * Information about the started client as sent from the server.
 * 
 * @author Andreas Holstenson
 *
 */
public class SessionStatus
{
	private final String id;

	public SessionStatus(String id)
	{
		this.id = id;
	}
	
	public String getId()
	{
		return id;
	}
	
	@Override
	public String toString()
	{
		return "SESSION " + id;
	}
}
