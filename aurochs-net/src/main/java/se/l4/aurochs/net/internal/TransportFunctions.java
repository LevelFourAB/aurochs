package se.l4.aurochs.net.internal;

import org.jboss.netty.channel.Channel;

/**
 * Functions used internally by the transport library.
 * 
 * @author Andreas Holstenson
 *
 */
public interface TransportFunctions
{
	TransportSession createSession(Channel channel, String id);
	
	void setupPipeline(TransportSession session, Channel channel);
}
