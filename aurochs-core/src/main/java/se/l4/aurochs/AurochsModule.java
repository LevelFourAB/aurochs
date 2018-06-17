package se.l4.aurochs;

import se.l4.aurochs.ConfigBinder;
import se.l4.crayon.CrayonModule;

/**
 * Abstract base for modules within an Aurochs application. Extends
 * {@link CrayonModule} and as such supports {@link se.l4.crayon.Contribution}s.
 */
public abstract class AurochsModule
	extends CrayonModule
{
	private ConfigBinder configBinder;


	/**
	 * Get the configuration binder for this module.
	 * 
	 * @return
	 *   instance of config binder
	 */
	protected ConfigBinder configBinder()
	{
		if(configBinder == null)
		{
			configBinder = ConfigBinder.newBinder(binder());
		}
		
		return configBinder;
	}

	/**
	 * Start binding a configuration value of the given type.
	 */
	protected <T> ConfigBinder.BindingBuilder<T> bindConfig(Class<T> configType)
	{
		return configBinder().bind(configType);
	}
}