package se.l4.aurochs.net.hosts;

import java.net.URI;
import java.util.Set;

/**
 * Set of host names. The set is used to connect to one or more servers with
 * support for changing which servers should be connected.
 * 
 * @author Andreas Holstenson
 *
 */
public interface HostSet
{
	/**
	 * Listener for changes to the hosts.
	 * 
	 * @author Andreas Holstenson
	 *
	 */
	interface Listener
	{
		/**
		 * The hosts have changed.
		 * 
		 * @param newHosts
		 */
		void hostsChanged(Set<URI> newHosts);
	}
	
	/**
	 * Get the current hosts.
	 * 
	 * @return
	 */
	Set<URI> list();
	
	/**
	 * Add a listener to this set.
	 * 
	 * @param listener
	 */
	void addListener(Listener listener);
	
	/**
	 * Remove a listener from this set.
	 * 
	 * @param listener
	 */
	void removeListener(Listener listener);
}
