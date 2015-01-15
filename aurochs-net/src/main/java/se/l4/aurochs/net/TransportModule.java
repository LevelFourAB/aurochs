package se.l4.aurochs.net;

import se.l4.aurochs.core.AutoLoad;
import se.l4.aurochs.net.internal.DefaultTransportFunctions;
import se.l4.aurochs.net.internal.ServerBuilderImpl;
import se.l4.aurochs.net.internal.ServerConnectionImpl;
import se.l4.aurochs.net.internal.TransportFunctions;
import se.l4.crayon.CrayonModule;

/**
 * Module that activates support for transport functions such as connecting
 * to a server.
 *  
 * @author Andreas Holstenson
 *
 */
@AutoLoad
public class TransportModule
	extends CrayonModule
{
	@Override
	protected void configure()
	{
		bind(ServerConnection.class).to(ServerConnectionImpl.class);
		bind(ServerBuilder.class).to(ServerBuilderImpl.class);
		bind(TransportFunctions.class).to(DefaultTransportFunctions.class);
	}
}
