package se.l4.aurochs.core;

import com.google.inject.Injector;

/**
 * System wide {@link Session session}.
 * 
 * @author Andreas Holstenson
 *
 */
public interface SystemSession
	extends Session
{
	/**
	 * Get the {@link Injector injector} for the system.
	 * 
	 * @return
	 */
	Injector getInjector();
}
