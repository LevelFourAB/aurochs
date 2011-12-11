package se.l4.aurochs.net.internal.handshake;

/**
 * Message that indicates that the actual session should now begin.
 * 
 * @author Andreas Holstenson
 *
 */
public class BeginSession
{
	private final String id;

	public BeginSession(String id)
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
		if(id == null)
		{
			return "BEGIN";
		}
		else
		{
			return "BEGIN " + id;
		}
	}
}
