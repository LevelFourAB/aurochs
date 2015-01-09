package se.l4.aurochs.net;

import java.util.concurrent.CompletableFuture;

import se.l4.aurochs.core.Session;

/**
 * Extension to {@link Session} for those session that are remote.
 * 
 * @author Andreas Holstenson
 *
 */
public interface RemoteSession
	extends Session
{
	/**
	 * Get if this session is connected.
	 * 
	 * @return
	 */
	boolean isConnected();
	
	/**
	 * Get a future that will trigger when this connection is first connected.
	 * 
	 * @return
	 */
	CompletableFuture<Void> connectionFuture();
}
