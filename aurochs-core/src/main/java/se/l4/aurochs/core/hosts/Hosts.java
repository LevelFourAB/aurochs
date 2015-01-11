package se.l4.aurochs.core.hosts;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import se.l4.aurochs.core.events.EventHandle;

/**
 * Set of host names. The set is used to connect to one or more servers with
 * support for changing which servers should be connected.
 * 
 * @author Andreas Holstenson
 *
 */
public interface Hosts
{
	/**
	 * Get the current hosts.
	 * 
	 * @return
	 */
	Collection<URI> list();
	
	/**
	 * Get the current hosts and listen for new ones.
	 * 
	 * @param consumer
	 * @return
	 */
	EventHandle listen(Consumer<HostEvent> consumer);
	
	/**
	 * Create a set for the specified hosts.
	 * 
	 * @param hosts
	 * @return
	 */
	static Hosts create(String... hosts)
	{
		List<URI> uris = new ArrayList<URI>();
		for(String s : hosts)
		{
			uris.add(URI.create(s));
		}
		
		return new ImmutableHosts(uris);
	}
	
	/**
	 * Create a set for the specified hosts.
	 * 
	 * @param hosts
	 * @return
	 */
	static Hosts create(URI... hosts)
	{
		return new ImmutableHosts(hosts);
	}
}
