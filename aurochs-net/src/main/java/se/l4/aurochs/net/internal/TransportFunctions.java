package se.l4.aurochs.net.internal;

import io.netty.channel.Channel;

import java.util.concurrent.Executor;

/**
 * Functions used internally by the transport library.
 * 
 * @author Andreas Holstenson
 *
 */
public interface TransportFunctions
{
	TransportSession createSession(Channel channel, String id);
	
	void setupPipeline(Executor messageExecutor, TransportSession session, Channel channel);
}
