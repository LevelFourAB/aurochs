package se.l4.aurochs;

import se.l4.aurochs.internal.AutoLoaderImpl;
import se.l4.crayon.CrayonModule;

/**
 * Module that activates the core functionality of Aurochs.
 */
public class CoreModule
	extends CrayonModule
{

	@Override
	protected void configure()
	{
		bind(AutoLoader.class).to(AutoLoaderImpl.class);	
	}
	
}