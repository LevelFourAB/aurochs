package se.l4.aurochs.core;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLogger;

import se.l4.aurochs.config.ConfigBuilder;
import se.l4.aurochs.core.internal.InternalModule;
import se.l4.aurochs.core.internal.SystemSessionImpl;
import se.l4.aurochs.serialization.DefaultSerializerCollection;
import se.l4.aurochs.serialization.SerializerCollection;
import se.l4.crayon.Configurator;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;

/**
 * Application startup, uses Guice {@link Module modules}, configures them
 * and returns a {@link SystemSession system session}.
 * 
 * @author Andreas Holstenson
 *
 */
public class Application
{
	private final Logger logger;
	private final Configurator configurator;
	private final SerializerCollection collection;
	private final ConfigBuilder configBuilder;
	
	/**
	 * Start an application in {@link Stage#PRODUCTION}.
	 * 
	 */
	public Application()
	{
		this(Stage.PRODUCTION);
	}

	/**
	 * Start an application in the specified stage.
	 * 
	 * @param stage
	 */
	public Application(Stage stage)
	{
		this(new Configurator(stage));
	}
	
	/**
	 * Start an application on the specified configurator. Useful when
	 * integrating with other frameworks.
	 * 
	 * @param configurator
	 */
	public Application(Configurator configurator)
	{
		this.configurator = configurator;
		
		logger = LoggerFactory.getLogger(Application.class);
		configurator.setLogger(logger);
		
		collection = new DefaultSerializerCollection();
		configBuilder = ConfigBuilder.with(collection);
	}

	/**
	 * Set that the application should be created on top of another injector.
	 * 
	 * @param injector
	 * @return
	 */
	public Application withParentInjector(Injector injector)
	{
		configurator.setParentInjector(injector);
		
		return this;
	}
	
	/**
	 * Add a configuration file to the application.
	 * 
	 * @param file
	 * @return
	 */
	public Application withConfigFile(String file)
	{
		configBuilder.addFile(file);
		
		return this;
	}
	
	/**
	 * Add a configuration file to the application.
	 * 
	 * @param file
	 * @return
	 */
	public Application withConfigFile(File file)
	{
		configBuilder.addFile(file);
		
		return this;
	}

	/**
	 * Add a new module to the application.
	 * 
	 * @param instance
	 * @return
	 */
	public Application add(Module instance)
	{
		configurator.add(instance);
		
		return this;
	}

	/**
	 * Add a new module to the application. The module will be created via
	 * reflection and needs to have a public default constructor.
	 * 
	 * @param module
	 * @return
	 */
	public Application add(Class<? extends Module> module)
	{
		configurator.add(module);
		
		return this;
	}
	
	/**
	 * Start the application and return the system session.
	 * 
	 * @return
	 */
	public SystemSession start()
	{
		/*
		 * Create the actual injector. Temporarily suppress logging while
		 * adding the internal module.
		 */
		Injector injector = configurator
			.setLogger(NOPLogger.NOP_LOGGER)
			.add(new InternalModule(collection, configBuilder.build()))
			.setLogger(logger)
			.configure();
		
		return new SystemSessionImpl(injector);
	}
}
