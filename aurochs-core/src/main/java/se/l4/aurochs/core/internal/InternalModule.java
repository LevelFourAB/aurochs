package se.l4.aurochs.core.internal;

import se.l4.aurochs.config.Config;
import se.l4.aurochs.core.spi.Sessions;
import se.l4.aurochs.serialization.SerializerCollection;
import se.l4.crayon.CrayonModule;

/**
 * Module that binds up internal services.
 * 
 * @author Andreas Holstenson
 *
 */
public class InternalModule
	extends CrayonModule
{
	private final SerializerCollection collection;
	private final Config config;

	public InternalModule(SerializerCollection collection, Config config)
	{
		this.collection = collection;
		this.config = config;
	}
	
	@Override
	protected void configure()
	{
		bind(Config.class).toInstance(config);
		bind(SerializerCollection.class).toInstance(collection);
		
		bind(Sessions.class).to(SessionsImpl.class);
	}
}
