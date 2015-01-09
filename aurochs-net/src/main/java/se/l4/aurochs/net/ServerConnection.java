package se.l4.aurochs.net;

import java.net.URI;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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
	 * Set how to handle TLS.
	 * 
	 * @author Andreas Holstenson
	 *
	 */
	enum TLSMode
	{
		/** Automatic, turn on if available (default). */
		AUTOMATIC,
		/** Force the usage of TLS. */
		FORCE,
		/** Never use TLS. */
		NEVER
	}
	
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
	 * Set how to use TLS. By default this is set to {@link TLSMode#AUTOMATIC automatic}
	 * meaning the client will use encryption if is available.
	 * 
	 * @param mode
	 * @return
	 */
	ServerConnection setTLS(TLSMode mode);
	
	/**
	 * Set the trust manager to use. This is usually an instance of
	 * {@link X509TrustManager}. By default the client will trust any
	 * certificate.
	 * 
	 * @param trustManager
	 * @return
	 */
	ServerConnection setTrustManager(TrustManager trustManager);
	
	/**
	 * Start the connection, returning a new session. This method will not block and wait for
	 * the connection to be established. Use {@link RemoteSession#connectionFuture()} to wait for
	 * the connection.
	 * 
	 * @return
	 * @throws ConnectionException
	 */
	RemoteSession connect();
}
