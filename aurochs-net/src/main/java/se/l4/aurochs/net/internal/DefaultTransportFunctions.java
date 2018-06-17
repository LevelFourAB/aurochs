package se.l4.aurochs.net.internal;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Injector;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateHandler;
import se.l4.aurochs.net.internal.handlers.ByteMessageDecoder;
import se.l4.aurochs.net.internal.handlers.ByteMessageEncoder;
import se.l4.aurochs.net.internal.handlers.MessagingHandler;
import se.l4.aurochs.net.internal.handlers.VarintFrameDecoder;
import se.l4.aurochs.net.internal.handlers.VarintLengthPrepender;
import se.l4.aurochs.sessions.Session;
import se.l4.aurochs.sessions.Sessions;
import se.l4.commons.io.ByteMessage;
import se.l4.commons.io.Bytes;
import se.l4.commons.io.DefaultByteMessage;
import se.l4.commons.serialization.SerializerCollection;
import se.l4.commons.serialization.standard.CompactDynamicSerializer;

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
	public se.l4.aurochs.channels.Channel<Object> createObjectChannel(se.l4.aurochs.channels.Channel<ByteMessage> raw)
	{
		return raw
			.filter(ByteMessage.tag(1))
			.transform(
				o -> new DefaultByteMessage(1, Bytes.create(serializer.toBytes(o))),
				o -> {
					try {
						return serializer.fromBytes(o.getData().toByteArray());
					} catch(IOException e) {
						throw Throwables.propagate(e);
					}
				}
			);
	}

	@Override
	public void setupPipeline(Executor messageExecutor, Consumer<ByteMessage> messageReceiver, Channel channel)
	{
		ChannelPipeline pipeline = channel.pipeline();
		
		// Remove handshake
		pipeline.remove("handshake");
		pipeline.remove("handshakeDecoder");
		pipeline.remove("handshakeEncoder");
		
		// Decoders
		pipeline.addLast("frameDecoder", new VarintFrameDecoder());
		pipeline.addLast("decoder", new ByteMessageDecoder());
		
		// Encoders
		pipeline.addLast("frameEncoder", new VarintLengthPrepender());
		pipeline.addLast("encoder", new ByteMessageEncoder());
		
		// Idle state
		pipeline.addLast("idleStateHandler", new IdleStateHandler(15, 5, 0));
		
		// Actual handler
		pipeline.addLast("messaging", new MessagingHandler(messageExecutor, messageReceiver));
	}
	
	@Override
	public void markSessionReady(Session session)
	{
		// "Create" the session
		sessions.create(session);
	}
}
