package se.l4.aurochs.transport.internal.handshake;

/**
 * Message for available capabilities on a server.
 * 
 * @author Andreas Holstenson
 *
 */
public class Capabilities
{
	private final String[] values;

	public Capabilities(String... values)
	{
		this.values = values;
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("CAPS");
		
		for(String s : values)
		{
			builder
				.append(' ')
				.append(s);
		}
		
		return builder.toString();
	}
}
