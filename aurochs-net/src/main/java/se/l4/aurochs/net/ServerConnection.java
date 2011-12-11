package se.l4.aurochs.net;

import java.net.URI;

import se.l4.aurochs.core.Session;
import se.l4.aurochs.net.hosts.HostSet;

/**
 * Creator of a connection to a server.
 * 
 * @author Andreas Holstenson
 *
 */
public interface ServerConnection
{
	/**
	 * Set the host to connect to.
	 * 
	 * @param uri
	 * @return
	 */
	ServerConnection setHost(String uri);
	
	/**
	 * Set the host to connect to.
	 * 
	 * @param uri
	 * @return
	 */
	ServerConnection setHost(URI uri);
	
	/**
	 * Set a number of hosts to connect to.
	 * 
	 * @param hosts
	 * @return
	 */
	ServerConnection setHosts(HostSet hosts);
	
	/**
	 * Establish the connection, blocking until it is established.
	 * 
	 * @return
	 * @throws ConnectionException
	 */
	Session connect()
		throws ConnectionException;
}
