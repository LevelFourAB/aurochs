package se.l4.aurochs.sessions;

import se.l4.aurochs.sessions.internal.SessionsImpl;
import se.l4.crayon.CrayonModule;

/**
 * Module for binding up {@link Sessions}.
 */
public class SessionsModule
	extends CrayonModule
{

	@Override
	protected void configure()
	{
		bind(Sessions.class).to(SessionsImpl.class);
	}

}