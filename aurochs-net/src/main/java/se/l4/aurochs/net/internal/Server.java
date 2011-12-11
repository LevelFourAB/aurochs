package se.l4.aurochs.net.internal;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;

import se.l4.aurochs.config.Config;
import se.l4.aurochs.net.internal.handlers.HandshakeDecoder;
import se.l4.aurochs.net.internal.handlers.HandshakeEncoder;
import se.l4.aurochs.net.internal.handlers.ServerHandler;
import se.l4.aurochs.net.internal.handlers.ServerHandshakeHandler;
import se.l4.crayon.services.ManagedService;

import com.google.inject.Inject;

/**
 * Server implementation.
 * 
 * @author Andreas Holstenson
 *
 */
public class Server
	implements ManagedService
{
	private final DefaultTransportFunctions functions;
	
	private final ServerConfig config;
	
	private final ChannelGroup group;
	
	private volatile Channel channel;
	private volatile ChannelFactory factory;

	@Inject
	public Server(Config config, DefaultTransportFunctions functions)
	{
		this.functions = functions;
		
		this.config = config.asObject("transport.server", ServerConfig.class);
		group = new DefaultChannelGroup("aurochs-server-" + this.config.getPort());
	}
	
	@Override
	public void start()
		throws Exception
	{
		if(channel != null) return;
		
		factory = new NioServerSocketChannelFactory(
			Executors.newCachedThreadPool(),
			Executors.newCachedThreadPool()
		);
		ServerBootstrap bootstrap = new ServerBootstrap(factory);

		bootstrap.setPipelineFactory(new ChannelPipelineFactory()
		{
			public ChannelPipeline getPipeline() throws Exception
			{
				ChannelPipeline pipeline = Channels.pipeline();
				
				pipeline.addLast("handshakeDecoderByLine", new DelimiterBasedFrameDecoder(4096, Delimiters.lineDelimiter()));
				pipeline.addLast("handshakeDecoder", new HandshakeDecoder());
				pipeline.addLast("handshakeEncoder", new HandshakeEncoder());
				
				pipeline.addLast("handshake", new ServerHandshakeHandler(functions));
				
				pipeline.addLast("server", new ServerHandler(group));
				
				return pipeline;
			}
		});
		
		channel = bootstrap.bind(new InetSocketAddress(config.getPort()));
		group.add(channel);
	}
	
	@Override
	public void stop()
		throws Exception
	{
		if(channel != null)
		{
			ChannelGroupFuture future = group.close();
			future.awaitUninterruptibly();
			factory.releaseExternalResources();
			channel = null;
		}
	}
}
