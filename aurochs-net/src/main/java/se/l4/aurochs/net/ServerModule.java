package se.l4.aurochs.net;

import com.google.inject.name.Named;

import se.l4.crayon.Contribution;
import se.l4.crayon.CrayonModule;
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

	@Contribution
	@Named("aurochs-server")
	public void contributeServer(ServiceManager services, ServerBuilder builder)
	{
		services.addService(builder.withConfig("net.config").build());
	}
}
