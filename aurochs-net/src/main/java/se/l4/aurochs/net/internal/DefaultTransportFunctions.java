package se.l4.aurochs.net.internal;

import java.util.concurrent.Executor;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;

import se.l4.aurochs.core.spi.Sessions;
import se.l4.aurochs.net.internal.handlers.ByteMessageDecoder;
import se.l4.aurochs.net.internal.handlers.ByteMessageEncoder;
import se.l4.aurochs.net.internal.handlers.MessagingHandler;
import se.l4.aurochs.net.internal.handlers.VarintFrameDecoder;
import se.l4.aurochs.net.internal.handlers.VarintLengthPrepender;
import se.l4.aurochs.serialization.SerializerCollection;
import se.l4.aurochs.serialization.standard.CompactDynamicSerializer;

import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * Default implementation of {@link TransportFunctions}.
 * 
 * @author Andreas Holstenson
 *
 */
public class DefaultTransportFunctions
	implements TransportFunctions
{
	private final Injector injector;
	private final Sessions sessions;
	private final CompactDynamicSerializer serializer;

	@Inject
	public DefaultTransportFunctions(Injector injector, 
			Sessions sessions, 
			SerializerCollection collection)
	{
		this.injector = injector;
		this.sessions = sessions;
		
		serializer = new CompactDynamicSerializer(collection);
	}

	@Override
	public TransportSession createSession(Channel channel, String id)
	{
		return new TransportSession(injector, channel, serializer);
	}

	@Override
	public void setupPipeline(Executor messageExecutor, TransportSession session, Channel channel)
	{
		ChannelPipeline pipeline = channel.getPipeline();
		
		// Remove handshake
		pipeline.remove("handshake");
		pipeline.remove("handshakeDecoderByLine");
		pipeline.remove("handshakeDecoder");
		
		
		// Decoders
		pipeline.addLast("frameDecoder", new VarintFrameDecoder());
		pipeline.addLast("decoder", new ByteMessageDecoder());
		
		// Encoders
		pipeline.addLast("frameEncoder", new VarintLengthPrepender());
		pipeline.addLast("encoder", new ByteMessageEncoder());
		
		// Actual handler
		pipeline.addLast("messaging", new MessagingHandler(messageExecutor, session));
		
		// "Create" the session
		sessions.create(session);
	}
}
