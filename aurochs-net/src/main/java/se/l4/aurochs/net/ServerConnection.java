package se.l4.aurochs.net;

import java.net.URI;
import java.util.function.Consumer;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import se.l4.aurochs.core.channel.Channel;
import se.l4.aurochs.core.hosts.Hosts;
import se.l4.aurochs.core.io.ByteMessage;

/**
 * Creator of a connection to a server.
 * 
 * @author Andreas Holstenson
 *
 */
public interface ServerConnection
{
	static final int DEFAULT_PORT = 7400;
	
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
	ServerConnection setHosts(Hosts hosts);
	
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
	 * Set an initializer that will run everytime a new channel is created for the remote
	 * connection.
	 * 
	 * @param initializer
	 * @return
	 */
	ServerConnection setChannelInitializer(Consumer<Channel<ByteMessage>> initializer);
	
	/**
	 * Set the minimum amount of connections to keep to the server.
	 * 
	 * @param minConnections
	 * @return
	 */
	ServerConnection setMinConnections(int minConnections);
	
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
