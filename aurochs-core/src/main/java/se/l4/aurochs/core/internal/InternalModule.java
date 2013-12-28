package se.l4.aurochs.core.internal;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.List;

import se.l4.aurochs.config.Config;
import se.l4.aurochs.config.ConfigBuilder;
import se.l4.aurochs.core.SerializerRegistration;
import se.l4.aurochs.core.spi.Sessions;
import se.l4.aurochs.serialization.DefaultSerializerCollection;
import se.l4.aurochs.serialization.SerializerCollection;
import se.l4.aurochs.serialization.spi.InstanceFactory;
import se.l4.crayon.Contributions;
import se.l4.crayon.CrayonModule;
import se.l4.crayon.services.ServicesModule;

import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * Module that binds up internal services.
 * 
 * @author Andreas Holstenson
 *
 */
public class InternalModule
	extends CrayonModule
{
	private List<File> configFiles;

	public InternalModule(List<File> configFiles)
	{
		this.configFiles = configFiles;
	}
	
	@Override
	protected void configure()
	{
		install(new ServicesModule());
		
		bind(Sessions.class).to(SessionsImpl.class);
		
		bindContributions(SerializerRegistration.class);
	}
	
	@Provides
	@Singleton
	public SerializerCollection provideSerializerCollection(final Injector injector)
	{
		return new DefaultSerializerCollection(new InstanceFactory()
		{
			@Override
			public <T> T create(Class<T> type, Annotation[] annotations)
			{
				return injector.getInstance(type);
			}
			
			@Override
			public <T> T create(Class<T> type)
			{
				return injector.getInstance(type);
			}
		});
	}
	
	@Provides
	@Singleton
	public Config provideConfig(SerializerCollection collection,
			@SerializerRegistration Contributions contributions)
	{
		contributions.run();
		
		ConfigBuilder builder = ConfigBuilder.with(collection);
		for(File f : configFiles)
		{
			builder.addFile(f);
		}
		return builder.build();
	}
}
