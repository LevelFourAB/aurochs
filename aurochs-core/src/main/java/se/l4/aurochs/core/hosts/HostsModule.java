package se.l4.aurochs.core.hosts;

import se.l4.aurochs.core.SerializerRegistration;
import se.l4.aurochs.serialization.SerializerCollection;
import se.l4.crayon.CrayonModule;

/**
 * Module for {@link Hosts}.
 * @author Andreas Holstenson
 *
 */
public class HostsModule
	extends CrayonModule
{

	@Override
	protected void configure()
	{
	}

	@SerializerRegistration
	public void register(SerializerCollection serializers)
	{
		serializers.bind(Hosts.class, new HostsSerializer());
	}
}
