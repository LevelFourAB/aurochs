package se.l4.aurochs.transport;

import se.l4.aurochs.core.Session;

/**
 * Creator of a connection to a server.
 * 
 * @author Andreas Holstenson
 *
 */
public interface ServerConnection
{
	/**
	 * Set the address to connect to.
	 * 
	 * @param hostname
	 * @param port
	 * @return
	 */
	ServerConnection setAddress(String hostname, int port);
	
	/**
	 * Establish the connection, blocking until it is established.
	 * 
	 * @return
	 * @throws ConnectionException
	 */
	Session connect()
		throws ConnectionException;
}
