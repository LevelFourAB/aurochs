package se.l4.aurochs.net;

import se.l4.aurochs.net.internal.ServerConnectionImpl;
import se.l4.crayon.CrayonModule;

/**
 * Module that activates support for transport functions such as connecting
 * to a server.
 *  
 * @author Andreas Holstenson
 *
 */
public class TransportModule
	extends CrayonModule
{
	@Override
	protected void configure()
	{
		bind(ServerConnection.class).to(ServerConnectionImpl.class);
	}
}