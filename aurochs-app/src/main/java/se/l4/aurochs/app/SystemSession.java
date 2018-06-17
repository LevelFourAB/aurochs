package se.l4.aurochs.app;

import com.google.inject.Injector;

import se.l4.aurochs.sessions.Session;

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
