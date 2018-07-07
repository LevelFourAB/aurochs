package se.l4.aurochs.app.internal;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.List;

import javax.validation.Validation;

import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import se.l4.aurochs.CoreModule;
import se.l4.aurochs.SerializerRegistration;
import se.l4.aurochs.hosts.HostsModule;
import se.l4.commons.config.Config;
import se.l4.commons.config.ConfigBuilder;
import se.l4.commons.config.internal.FileSerializer;
import se.l4.commons.guice.InstanceFactoryModule;
import se.l4.commons.id.LongIdGenerator;
import se.l4.commons.id.SimpleLongIdGenerator;
import se.l4.commons.serialization.DefaultSerializerCollection;
import se.l4.commons.serialization.SerializerCollection;
import se.l4.commons.types.InstanceFactory;
import se.l4.crayon.Contributions;
import se.l4.crayon.CrayonModule;
import se.l4.crayon.services.ServicesModule;
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
		install(new InstanceFactoryModule());
		install(new CoreModule());
		install(new ServicesModule());
		install(new HostsModule());
		
		bindContributions(SerializerRegistration.class);
		
		bind(LongIdGenerator.class).to(SimpleLongIdGenerator.class);
	}
	
	@Provides
	@Singleton
	public SerializerCollection provideSerializerCollection(InstanceFactory factory)
	{
		DefaultSerializerCollection result = new DefaultSerializerCollection(factory);
		result.bind(File.class, new FileSerializer(new File("").getAbsoluteFile()));
		
		return result;
	}
	
	@Provides
	@Singleton
	public Config provideConfig(SerializerCollection collection,
			@SerializerRegistration Contributions contributions)
	{
		contributions.run();
		
		ConfigBuilder builder = Config.builder()
			.withSerializerCollection(collection)
			.withValidatorFactory(Validation.buildDefaultValidatorFactory());
		
		for(File f : configFiles)
		{
			builder.addFile(f);
		}
		return builder.build();
	}
}
