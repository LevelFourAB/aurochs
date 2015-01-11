package se.l4.aurochs.net.internal;

import io.netty.bootstrap.ChannelFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLEngine;

import se.l4.aurochs.config.Config;
import se.l4.aurochs.net.internal.handlers.HandshakeDecoder;
import se.l4.aurochs.net.internal.handlers.HandshakeEncoder;
import se.l4.aurochs.net.internal.handlers.ServerHandshakeHandler;
import se.l4.crayon.services.ManagedService;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Server implementation.
 * 
 * @author Andreas Holstenson
 *
 */
public class Server
	implements ManagedService
{
	private final TransportFunctions functions;
	
	private final ServerConfig config;
	
//	private final ChannelGroup group;
	
	private volatile Channel channel;
	private volatile ChannelFactory factory;
	private volatile ThreadPoolExecutor executor;

	@Inject
	public Server(Config config, DefaultTransportFunctions functions)
	{
		this.functions = functions;
		
		this.config = config.get("net.server", ServerConfig.class)
			.getOrDefault(new ServerConfig());
//		group = new DefaultChannelGroup("aurochs-server-" + this.config.getPort());
	}
	
	@Override
	public void start()
		throws Exception
	{
		if(channel != null) return;
		
		// The executor for messages
		executor = new ThreadPoolExecutor(
			config.getMinThreads(),
			config.getMaxThreads(), 
			60, TimeUnit.SECONDS, 
			new LinkedBlockingDeque<Runnable>(config.getQueueSize()), 
			new ThreadFactoryBuilder()
				.setNameFormat("aurochs-server-%s")
				.build()
			);
		
		EventLoopGroup bossGroup = new NioEventLoopGroup(2);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		
		final Provider<SSLEngine> engines = config.getTls() != null 
			? SslHelper.createEngine(config.getTls())
			: null;
			
		ServerBootstrap bootstrap = new ServerBootstrap()
			.group(bossGroup, workerGroup)
			.channel(NioServerSocketChannel.class)
			.childHandler(new ChannelInitializer<Channel>()
			{
				@Override
				protected void initChannel(Channel ch)
					throws Exception
				{
					ChannelPipeline pipeline = ch.pipeline();
					
					pipeline.addLast("handshakeDecoder", new HandshakeDecoder());
					pipeline.addLast("handshakeEncoder", new HandshakeEncoder());
//					
					pipeline.addLast("handshake", new ServerHandshakeHandler(functions, executor, engines));
					
//					pipeline.addLast("server", new ServerHandler(group));
				}
			});
		
		ChannelFuture future = bootstrap.bind(new InetSocketAddress(config.getPort())).sync();
//		group.add(future.channel());
	}
	
	@Override
	public void stop()
		throws Exception
	{
		if(channel != null)
		{
			executor.shutdownNow();
			
//			ChannelGroupFuture future = group.close();
//			future.awaitUninterruptibly();
//			factory.releaseExternalResources();
			channel = null;
		}
	}
	
	@Override
	public String toString()
	{
		return "Server (port " + config.getPort() + ")";
	}
}
