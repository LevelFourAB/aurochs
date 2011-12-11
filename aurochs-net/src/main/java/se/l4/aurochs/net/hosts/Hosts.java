package se.l4.aurochs.net.hosts;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilities related to hosts and server connections.
 * 
 * @author Andreas Holstenson
 *
 */
public class Hosts
{
	private Hosts()
	{
	}
	
	/**
	 * Create a set for the specified hosts.
	 * 
	 * @param hosts
	 * @return
	 */
	public static HostSet create(String... hosts)
	{
		List<URI> uris = new ArrayList<URI>();
		for(String s : hosts)
		{
			uris.add(URI.create(s));
		}
		
		return new StaticHostSet(uris);
	}
	
	/**
	 * Create a set for the specified hosts.
	 * 
	 * @param hosts
	 * @return
	 */
	public static HostSet create(URI... hosts)
	{
		return new StaticHostSet(hosts);
	}
}
