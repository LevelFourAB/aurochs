package se.l4.aurochs.transport.internal;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;

import se.l4.aurochs.core.spi.Sessions;
import se.l4.aurochs.serialization.SerializerCollection;
import se.l4.aurochs.serialization.format.BinaryInput;
import se.l4.aurochs.serialization.format.BinaryOutput;
import se.l4.aurochs.serialization.format.StreamingInput;
import se.l4.aurochs.serialization.format.StreamingOutput;
import se.l4.aurochs.serialization.standard.CompactDynamicSerializer;
import se.l4.aurochs.transport.internal.handlers.MessagingHandler;
import se.l4.aurochs.transport.internal.handlers.SerializationDecoder;
import se.l4.aurochs.transport.internal.handlers.SerializationEncoder;
import se.l4.aurochs.transport.internal.handlers.VarintFrameDecoder;
import se.l4.aurochs.transport.internal.handlers.VarintLengthPrepender;

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
	private final StreamFactory streamFactory;
	
	private final CompactDynamicSerializer serializer;

	@Inject
	public DefaultTransportFunctions(Injector injector, 
			Sessions sessions, 
			SerializerCollection collection)
	{
		this.injector = injector;
		this.sessions = sessions;
		serializer = new CompactDynamicSerializer(collection);
		this.streamFactory = new StreamFactory()
		{
			@Override
			public StreamingOutput createOutput(ChannelBuffer buffer)
			{
				return new BinaryOutput(new ChannelBufferOutputStream(buffer));
			}
			
			@Override
			public StreamingInput createInput(ChannelBuffer buffer)
			{
				return new BinaryInput(new ChannelBufferInputStream(buffer));
			}
		};
	}

	@Override
	public TransportSession createSession(Channel channel, String id)
	{
		return new TransportSession(injector, channel);
	}

	public MessagingHandler createHandler(TransportSession session)
	{
		return new MessagingHandler(session);
	}

	@Override
	public void setupPipeline(TransportSession session, Channel channel)
	{
		ChannelPipeline pipeline = channel.getPipeline();
		
		// Remove handshake
		pipeline.remove("handshake");
		pipeline.remove("handshakeDecoderByLine");
		pipeline.remove("handshakeDecoder");
		
		// Decoders
		pipeline.addLast("frameDecoder", new VarintFrameDecoder());
		pipeline.addLast("decoder", new SerializationDecoder(serializer, streamFactory));
		
		// Encoders
		pipeline.addLast("frameEncoder", new VarintLengthPrepender());
		pipeline.addLast("encoder", new SerializationEncoder(serializer, streamFactory));
		
		// Actual handler
		pipeline.addLast("messaging", new MessagingHandler(session));
		
		// "Create" the session
		sessions.create(session);
	}
}
