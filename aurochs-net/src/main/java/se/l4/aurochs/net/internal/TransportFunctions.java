package se.l4.aurochs.net.internal;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

import se.l4.aurochs.core.Session;
import se.l4.aurochs.core.channel.Channel;
import se.l4.commons.io.ByteMessage;

/**
 * Functions used internally by the transport library.
 * 
 * @author Andreas Holstenson
 *
 */
public interface TransportFunctions
{
	TransportSession createSession(io.netty.channel.Channel channel, String id);
	
	void setupPipeline(Executor messageExecutor, Consumer<ByteMessage> messageReceiver, io.netty.channel.Channel channel);
	
	void markSessionReady(Session session);
	
	Channel<Object> createObjectChannel(Channel<ByteMessage> raw);
}
