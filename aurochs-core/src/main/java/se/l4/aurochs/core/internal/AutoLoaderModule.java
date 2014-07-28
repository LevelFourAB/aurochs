package se.l4.aurochs.core.internal;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.l4.aurochs.core.AutoLoader;
import se.l4.aurochs.core.SerializerRegistration;
import se.l4.aurochs.serialization.SerializerCollection;
import se.l4.aurochs.serialization.Use;
import se.l4.crayon.CrayonModule;
import se.l4.crayon.annotation.Order;

import com.google.inject.Module;

/**
 * Module for plugin support.
 * 
 * @author Andreas Holstenson
 *
 */
public class AutoLoaderModule
	extends CrayonModule
{
	private final Set<String> packageNames;

	public AutoLoaderModule(Set<String> packageNames)
	{
		this.packageNames = packageNames;
	}

	@Override
	protected void configure()
	{
		Logger logger = LoggerFactory.getLogger(AutoLoaderModule.class);
		
		AutoLoaderImpl autoLoader = new AutoLoaderImpl(packageNames);
		bind(AutoLoader.class).toInstance(autoLoader);
		
		for(Class<? extends Module> m : autoLoader.getPluginsOfType(Module.class))
		{
			try
			{
				logger.info("Installing " + m);
				Module instance = m.newInstance();
				install(instance);
			}
			catch(Exception e)
			{
				throw new Error("Unable to create module " + m + "; " + e.getMessage(), e);
			}
		}
	}

	@SerializerRegistration(name="internal-serializers")
	@Order("last")
	public void autoRegisterSerializer(AutoLoader plugins, SerializerCollection collection)
	{
		Set<Class<?>> types = plugins.getClassesAnnotatedWith(Use.class);
		for(Class<?> c : types)
		{
			if(c.getTypeParameters().length == 0)
			{
				// Only register those classes that do not have any type params
				collection.bind(c);
			}
		}
	}
}
