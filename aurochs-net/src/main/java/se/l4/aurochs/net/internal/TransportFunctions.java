package se.l4.aurochs.net.internal;

import java.util.concurrent.Executor;

import se.l4.aurochs.core.channel.Channel;
import se.l4.aurochs.core.io.ByteMessage;

/**
 * Functions used internally by the transport library.
 * 
 * @author Andreas Holstenson
 *
 */
public interface TransportFunctions
{
	TransportSession createSession(io.netty.channel.Channel channel, String id);
	
	void setupPipeline(Executor messageExecutor, TransportSession session, io.netty.channel.Channel channel);
	
	Channel<Object> createObjectChannel(Channel<ByteMessage> raw);
}
