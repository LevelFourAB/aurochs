package se.l4.aurochs.sessions;

/**
 * Management functions for {@link Session sessions}.
 * 
 * @author Andreas Holstenson
 *
 */
public interface Sessions
{
	/**
	 * Listener for session events.
	 * 
	 * @author Andreas Holstenson
	 *
	 */
	interface Listener
	{
		/**
		 * A new session has been created.
		 * 
		 * @param session
		 */
		void sessionCreated(Session session);
		
		/**
		 * A previously established session has been destroyed and should
		 * no longer be used.
		 * 
		 * @param session
		 */
		void sessionDestroyed(Session session);
	}
	
	/**
	 * Add a new listener for sessions.
	 * 
	 * @param listener
	 */
	void addListener(Listener listener);
	
	/**
	 * Remove a listener.
	 * 
	 * @param listener
	 */
	void removeListener(Listener listener);
	
	/**
	 * Create a new session. Should only be called by code that actually
	 * creates the session instance, such as transport layers.
	 * 
	 * @param session
	 */
	void create(Session session);
	
	/**
	 * Destroy a session. Has the same restrictions as {@link #create(Session)}.
	 * 
	 * @param session
	 */
	void destroy(Session session);
	
	/**
	 * Activate a new session for the current thread. This method will return
	 * the old session, which should be activated when appropriate.
	 * 
	 * @param session
	 * @return
	 */
	Session activate(Session session);
	
	/**
	 * Get the active session for the current thread.
	 * 
	 * @return
	 */
	Session getActive();
}
