package se.l4.aurochs.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLogger;

import se.l4.aurochs.config.ConfigException;
import se.l4.aurochs.core.internal.AutoLoaderModule;
import se.l4.aurochs.core.internal.InternalModule;
import se.l4.aurochs.core.internal.SystemSessionImpl;
import se.l4.crayon.Configurator;

import com.google.common.base.Joiner;
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
	private final List<File> configFiles;
	private final Set<String> packages;
	private String identifier;
	
	/**
	 * Get the default stage. This will look for a system property named
	 * production that can be set to false to run in development mode.
	 * 
	 * @return
	 */
	private static Stage getDefaultStage()
	{
		String productionStr = System.getProperty("production");
		boolean production = ! "false".equals(productionStr);
		return production ? Stage.PRODUCTION : Stage.DEVELOPMENT;
	}
	
	/**
	 * Start an application in {@link Stage#PRODUCTION}.
	 * 
	 */
	public Application()
	{
		this(getDefaultStage());
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
		logger.info("Creating with stage " + configurator.getStage());
		configurator.setLogger(logger);
		
		configFiles = new ArrayList<>();
		packages = new HashSet<>();
		packages.add("se.l4.aurochs");
	}
	
	/**
	 * Set the identifier of this application. Doing this will activate
	 * automatic locating of a default configuration file. If such a file
	 * can not be found the application will not start.
	 * 
	 * @param identifier
	 * @return
	 */
	public Application setIdentifier(String identifier)
	{
		this.identifier = identifier;
		
		return withConfigFile(findDefaultConfigFile());
	}

	/**
	 * Add a configuration file to the application.
	 * 
	 * @param file
	 * @return
	 */
	public Application withConfigFile(String file)
	{
		return withConfigFile(findConfigFile(file, true));
	}
	
	/**
	 * Add a configuration file to the application.
	 * 
	 * @param file
	 * @return
	 */
	public Application withConfigFile(File file)
	{
		if(! file.exists())
		{
			throw new ConfigException("The file " + file + " does not exist");
		}
		
		configFiles.add(file);
		
		return this;
	}
	
	private File findDefaultConfigFile()
	{
		try
		{
			// First look in JNDI context
			Context ctx = new InitialContext();
			String value = (String) ctx.lookup("java:comp/env/" + identifier + "/config");
			
			if(value != null)
			{
				File file = new File(value.toString());
				if(! file.exists())
				{
					throw new ConfigException("App was configured via JDNI to use the file `" + file + "`, but it does not exist");
				}
				
				return file;
			}
		}
		catch(NamingException e)
		{
		}
		
		String[] files = { "/etc/" + identifier + "/default.conf", identifier + ".conf", "default.conf" };
		
		for(String f : files)
		{
			File file = new File(f);
			if(file.exists())
			{
				return file;
			}
		}
		
		throw new ConfigException("Unable to find configuration. Looked for " + Joiner.on(", ").join(files)); 
	}
	
	protected File findConfigFile(String fileName, boolean required)
	{
		String[] files = { "/etc/" + identifier + "/" + fileName, fileName };
		
		for(String f : files)
		{
			File file = new File(f);
			if(file.exists())
			{
				return file;
			}
		}
		
		if(required)
		{
			throw new ConfigException("Unable to find configuration. Looked for " + Joiner.on(", ").join(files));
		}
		
		return null;
	}
	
	/**
	 * Add a package that should have its {@link Module}s autoloaded if they
	 * are annotated with {@link AutoLoad}.
	 * 
	 * @param pkg
	 * @return
	 */
	public Application withPackage(String pkg)
	{
		packages.add(pkg);
		
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
		// Setup uncaught exception handling
		Thread.setDefaultUncaughtExceptionHandler((thread, t) ->
			logger.warn("Unhandled exception in thread " + thread.getName() + ": " + t.getMessage(), t)
		);
		
		InternalModule internalModule = new InternalModule(configFiles);
		Injector injector = configurator
			.setLogger(NOPLogger.NOP_LOGGER)
			.add(internalModule)
			.add(new AutoLoaderModule(packages))
			.setLogger(logger)
			.configure();
		
		return new SystemSessionImpl(injector);
	}
}
