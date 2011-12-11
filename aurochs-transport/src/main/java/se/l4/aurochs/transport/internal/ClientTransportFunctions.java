package se.l4.aurochs.transport.internal;

import org.jboss.netty.channel.Channel;

import se.l4.aurochs.core.spi.Sessions;
import se.l4.aurochs.serialization.SerializerCollection;

import com.google.common.util.concurrent.SettableFuture;
import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * Client specific {@link TransportFunctions}.
 * 
 * @author Andreas Holstenson
 *
 */
public class ClientTransportFunctions
	extends DefaultTransportFunctions
{
	private final SettableFuture<ConnectionResult> future;

	@Inject
	public ClientTransportFunctions(Injector injector, Sessions sessions, SerializerCollection collection)
	{
		super(injector, sessions, collection);
		
		future = SettableFuture.create();
	}

	public void raiseConnectionError(Throwable t)
	{
		future.set(new ConnectionResult(t));
	}

	@Override
	public void setupPipeline(TransportSession session, Channel channel)
	{
		super.setupPipeline(session, channel);
		
		// Pipeline is setup, session is now usable
		future.set(new ConnectionResult(session));
	}
	
	public SettableFuture<ConnectionResult> getFuture()
	{
		return future;
	}
}
