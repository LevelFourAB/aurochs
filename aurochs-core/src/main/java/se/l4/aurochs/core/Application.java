package se.l4.aurochs.core;

import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLogger;

import se.l4.aurochs.core.internal.InternalModule;
import se.l4.aurochs.core.internal.SystemSessionImpl;
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
	private final Configurator configurator;
	
	public Application()
	{
		this(Stage.PRODUCTION);
	}

	public Application(Stage stage)
	{
		configurator = new Configurator(stage)
			.setLogger(NOPLogger.NOP_LOGGER)
			.add(new InternalModule())
			.setLogger(LoggerFactory.getLogger(Application.class));
	}

	public Application setParentInjector(Injector injector)
	{
		configurator.setParentInjector(injector);
		
		return this;
	}

	public Application add(Module instance)
	{
		configurator.add(instance);
		
		return this;
	}

	public Application add(Class<? extends Module> module)
	{
		configurator.add(module);
		
		return this;
	}
	
	public SystemSession start()
	{
		Injector injector = configurator.configure();
		
		return new SystemSessionImpl(injector);
	}
}
