package se.l4.aurochs.net.internal;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.net.ssl.TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.l4.aurochs.core.channel.MessageEvent;
import se.l4.aurochs.core.io.ByteMessage;
import se.l4.aurochs.core.spi.AbstractChannel;
import se.l4.aurochs.net.ServerConnection;
import se.l4.aurochs.net.ServerConnection.TLSMode;
import se.l4.aurochs.net.internal.handlers.ClientHandshakeHandler;
import se.l4.aurochs.net.internal.handlers.HandshakeDecoder;
import se.l4.aurochs.net.internal.handlers.HandshakeEncoder;

public class NettyClientChannel
	extends AbstractChannel<ByteMessage>
{
	private static final Logger log = LoggerFactory.getLogger(NettyClientChannel.class);
	
	private final EventLoopGroup group;
	
	private final TransportFunctions functions;
	private final ExecutorService executor;
	private final TLSMode tlsMode;
	private final TrustManager trustManager;
	private final Collection<URI> hosts;
	
	private final int connections;
	private final CompletableFuture<Void> connectionFuture;
	
	private volatile Channel[] channels;
	private final ChannelGroup channelGroup;

	private final ScheduledExecutorService scheduler;

	private final Consumer<se.l4.aurochs.core.channel.Channel<ByteMessage>> initializer;
	
	public NettyClientChannel(EventLoopGroup group, 
			TransportFunctions functions,
			ExecutorService executor,
			TLSMode tlsMode,
			TrustManager trustManager,
			Collection<URI> hosts,
			int minConnections,
			Consumer<se.l4.aurochs.core.channel.Channel<ByteMessage>> initializer)
	{
		this.group = group;
		this.functions = functions;
		this.executor = executor;
		this.tlsMode = tlsMode;
		this.trustManager = trustManager;
		this.hosts = hosts;
		this.initializer = initializer;
		
		scheduler = Executors.newScheduledThreadPool(1); // TODO: Should we share this between all connections?
		connections = minConnections;
		
		this.channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
		channels = new Channel[0];
		
		connectionFuture = new CompletableFuture<>();
	}
	
	public void start()
	{
		for(int i=0, n=connections; i<n; i++)
		{
			startConnectionToServer();
		}
	}
	
	private void startConnectionToServer()
	{
		connectToServer(1);
	}
	
	private void connectToServer(int attempt)
	{
		URI uri = hosts.iterator().next();
		InetSocketAddress address = new InetSocketAddress(
			uri.getHost(), 
			uri.getPort() > 0 ? uri.getPort() : ServerConnection.DEFAULT_PORT
		);
		
		log.debug("Trying to establish new connection to server {}, attempt {}", uri, attempt);
		
		CompletableFuture<Channel> channelFuture = new CompletableFuture<>();
		Bootstrap bootstrap = new Bootstrap()
			.group(group)
			.channel(NioSocketChannel.class)
			.option(ChannelOption.TCP_NODELAY, true)
			.option(ChannelOption.SO_KEEPALIVE, true)
			.handler(new ChannelInitializer<Channel>()
			{
				@Override
				protected void initChannel(Channel ch)
					throws Exception
				{
					ChannelPipeline pipeline = ch.pipeline();
					
					pipeline.addLast("handshakeDecoder", new HandshakeDecoder());
					pipeline.addLast("handshakeEncoder", new HandshakeEncoder());
					
					pipeline.addLast("handshake", new ClientHandshakeHandler(
						functions,
						executor,
						tlsMode,
						trustManager,
						in -> fireMessageReceived(new MessageEvent<ByteMessage>(NettyClientChannel.this, NettyClientChannel.this, in)),
						channelFuture
					));
					
				}
			});
		
		ChannelFuture future = bootstrap.connect(address);
		ScheduledFuture<?> retry = scheduler.schedule(() -> {
			future.cancel(false);
			scheduleConnectToServer(attempt + 1);
		}, 15, TimeUnit.SECONDS);
		
		future.addListener(f -> {
			if(! f.isSuccess())
			{
				retry.cancel(false);
				connectionFuture.completeExceptionally(f.cause());
				scheduleConnectToServer(attempt + 1);
			}
		});
		
		channelFuture.thenAccept(ch -> {
			retry.cancel(true);
			addChannel(ch);
		});
	}
	
	private void scheduleConnectToServer(int attempt)
	{
		long delay = attempt > 5 ? 15000 : Math.min(3000, 1000 * Math.max(1, ThreadLocalRandom.current().nextInt(1 << attempt)));
		scheduler.schedule(() -> connectToServer(attempt), delay, TimeUnit.MILLISECONDS);
	}
	
	public boolean isConnected()
	{
		return channels.length > 0;
	}
	
	public CompletableFuture<Void> getConnectionFuture()
	{
		return connectionFuture;
	}
	
	public void addChannel(Channel channel)
	{
		if(channelGroup.add(channel))
		{
			log.debug("Connection to {} established", channel.remoteAddress());
			synchronized(channelGroup)
			{
				Channel[] channels = Arrays.copyOf(this.channels, this.channels.length + 1);
				channels[this.channels.length] = channel;
				this.channels = channels;
			}
			
			// Setup a listener to remove this channel when it is closed
			channel.closeFuture().addListener(r -> this.removeChannel(channel));
			
			connectionFuture.complete(null);
			
			initializer.accept(new AbstractChannel<ByteMessage>()
			{
				@Override
				public void close()
				{
				}
				
				@Override
				public void send(ByteMessage message)
				{
					channel.writeAndFlush(message);
				}
			});
		}
	}
	
	private void removeChannel(Channel channel)
	{
		log.debug("Connection to {} lost", channel.remoteAddress());
		
		synchronized(channelGroup)
		{
			Channel[] channels = this.channels;
			int index = -1;
			for(int i=0, n=channels.length; i<n; i++)
			{
				if(channels[i] == channel)
				{
					index = i;
					break;
				}
			}
			
			if(index == -1)
			{
				// Nothing to do, no such listener
				return;
			}
			
			int length = channels.length;
			Channel[] result = new Channel[length - 1];
			System.arraycopy(channels, 0, result, 0, index);
			
			if(index < length - 1)
			{
				System.arraycopy(channels, index + 1, result, index, length - index - 1);
			}
			
			this.channels = result;
			
			if(result.length < connections)
			{
				scheduler.execute(this::startConnectionToServer);
			}
		}
	}
	
	@Override
	public void close()
	{
		try
		{
			channelGroup.close().sync();
			group.shutdownGracefully().sync();
		}
		catch(InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}
	}
	
	@Override
	protected void fireMessageReceived(MessageEvent<? extends ByteMessage> event)
	{
		super.fireMessageReceived(event);
	}
	
	@Override
	public void send(ByteMessage message)
	{
		Channel[] current = this.channels;
		if(current.length == 0)
		{
			// TODO: How do we handle cases where we have no connection?
			return;
		}
		
		int idx = ThreadLocalRandom.current().nextInt(this.channels.length);
		current[idx].writeAndFlush(message);
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "{" + Arrays.toString(channels) + "}";
	}
}
