package se.l4.aurochs.transport;

import se.l4.aurochs.transport.internal.Server;
import se.l4.crayon.CrayonModule;
import se.l4.crayon.annotation.Contribution;
import se.l4.crayon.services.ServiceManager;

/**
 * Module that will start a server as part of the application.
 * 
 * @author Andreas Holstenson
 *
 */
public class ServerModule
	extends CrayonModule
{

	@Override
	protected void configure()
	{
	}

	@Contribution(name="transport:server")
	public void contributeServer(ServiceManager manager, Server server)
	{
		manager.addService(server);
	}
}
