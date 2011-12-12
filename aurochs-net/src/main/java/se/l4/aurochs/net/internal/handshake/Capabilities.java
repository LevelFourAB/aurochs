package se.l4.aurochs.net.internal.handshake;

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
	
	/**
	 * Check if the given capability is available.
	 * 
	 * @param value
	 * @return
	 */
	public boolean has(String value)
	{
		for(String v : values)
		{
			if(value.equals(v))
			{
				return true;
			}
		}
		
		return false;
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
