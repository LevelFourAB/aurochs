package se.l4.aurochs.net.internal.handshake;

import java.util.Collection;

/**
 * Capabilities as selected by the client.
 * 
 * @author Andreas Holstenson
 *
 */
public class SelectCapabilities
{
	private final String[] values;

	public SelectCapabilities(Collection<String> values)
	{
		this(values.toArray(new String[values.size()]));
	}
	
	public SelectCapabilities(String... values)
	{
		this.values = values;
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT");
		
		for(String s : values)
		{
			builder
				.append(' ')
				.append(s);
		}
		
		return builder.toString();
	}
}
