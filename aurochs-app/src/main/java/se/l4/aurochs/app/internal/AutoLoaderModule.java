package se.l4.aurochs.app.internal;

import java.lang.reflect.Modifier;
import java.util.Set;

import com.google.inject.Module;
import com.google.inject.name.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.l4.aurochs.AutoLoad;
import se.l4.aurochs.AutoLoader;
import se.l4.aurochs.SerializerRegistration;
import se.l4.commons.serialization.SerializationException;
import se.l4.commons.serialization.SerializerCollection;
import se.l4.commons.serialization.Use;
import se.l4.commons.types.TypeFinder;
import se.l4.crayon.CrayonModule;
import se.l4.crayon.Order;

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

		TypeFinder typeFinder = TypeFinder.builder()
			.addPackages(packageNames)
			.build();

		bind(TypeFinder.class).toInstance(typeFinder);

		for(Class<?> c : typeFinder.getTypesAnnotatedWith(AutoLoad.class))
		{
			if(Module.class.isAssignableFrom(c))
			{
				try
				{
					logger.info("Installing " + c);
					Object instance = c.newInstance();
					install((Module) instance);
				}
				catch(Exception e)
				{
					throw new Error("Unable to create module " + c + "; " + e.getMessage(), e);
				}
			}
		}
	}

	@SerializerRegistration
	@Named("internal-serializers")
	@Order("last")
	public void autoRegisterSerializer(AutoLoader plugins, SerializerCollection collection)
	{
		Set<Class<?>> types = plugins.getTypesAnnotatedWith(Use.class);
		for(Class<?> c : types)
		{
			if(c.getTypeParameters().length == 0
				&& c.getDeclaredAnnotation(Use.class) != null
				&& ! Modifier.isAbstract(c.getModifiers()))
			{
				// Only register those classes that do not have any type params and that are directly annotated with @Use
				try
				{
					collection.bind(c);
				}
				catch(SerializationException e)
				{
					throw new SerializationException("Unable to register " + c + "; " + e.getMessage(), e);
				}
			}
		}
	}
}
