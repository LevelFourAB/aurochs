package se.l4.aurochs.net;

import java.util.function.Consumer;

import se.l4.aurochs.net.ServerConfig;
import se.l4.aurochs.sessions.Session;
import se.l4.aurochs.sessions.Sessions;

/**
 * Builder for creating instances of {@link Server}.
 * 
 * @author Andreas Holstenson
 *
 */
public interface ServerBuilder
{
	/**
	 * Grab the configuration for the server from the given key.
	 * 
	 * @param key
	 * @return
	 */
	ServerBuilder withConfig(String key);
	
	/**
	 * Use the given configuration to configure the server.
	 * 
	 * @param config
	 * @return
	 */
	ServerBuilder withConfig(ServerConfig config);
	
	/**
	 * Add a session listener to use for this server.
	 * 
	 * @param listener
	 * @return
	 */
	ServerBuilder withSessionListener(Sessions.Listener listener);
	
	/**
	 * Add a session listener, alternative to {@link #withSessionListener(se.l4.aurochs.core.spi.Sessions.Listener)}.
	 * 
	 * @param onCreate
	 * @param onDestroy
	 * @return
	 */
	ServerBuilder withSessionListener(Consumer<Session> onCreate, Consumer<Session> onDestroy);
	
	/**
	 * Create a server and start it at the same time.
	 * 
	 * @return
	 */
	Server start()
		throws Exception;
	
	/**
	 * Just create the server, without starting it.
	 * 
	 * @return
	 */
	Server build();
}
