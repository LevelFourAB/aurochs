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

import javax.net.ssl.TrustManager;

import se.l4.aurochs.core.io.ByteMessage;
import se.l4.aurochs.core.spi.AbstractChannel;
import se.l4.aurochs.net.ServerConnection.TLSMode;
import se.l4.aurochs.net.internal.handlers.ClientHandshakeHandler;
import se.l4.aurochs.net.internal.handlers.HandshakeDecoder;
import se.l4.aurochs.net.internal.handlers.HandshakeEncoder;

public class NettyClientChannel
	extends AbstractChannel<ByteMessage>
{
	private final EventLoopGroup group;
	
	private final ClientTransportFunctions functions;
	private final ExecutorService executor;
	private final TLSMode tlsMode;
	private final TrustManager trustManager;
	private final Collection<URI> hosts;
	
	private final int connections;
	private final CompletableFuture<Void> connectionFuture;
	
	private volatile Channel[] channels;
	private final ChannelGroup channelGroup;
	
	public NettyClientChannel(EventLoopGroup group, 
			ClientTransportFunctions functions,
			ExecutorService executor,
			TLSMode tlsMode,
			TrustManager trustManager,
			Collection<URI> hosts)
	{
		this.group = group;
		this.functions = functions;
		this.executor = executor;
		this.tlsMode = tlsMode;
		this.trustManager = trustManager;
		this.hosts = hosts;
		
		connections = 1;
		
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
					
					pipeline.addLast("handshake", new ClientHandshakeHandler(functions, executor, tlsMode, trustManager, NettyClientChannel.this));
				}
			});
		
		URI uri = hosts.iterator().next();
		InetSocketAddress address = new InetSocketAddress(
			uri.getHost(), 
			uri.getPort() > 0 ? uri.getPort() : 7400
		);
		
		ChannelFuture future = bootstrap.connect(address);
		future.addListener(f -> {
			if(! f.isSuccess())
			{
				connectionFuture.completeExceptionally(f.cause());
			}
		});
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
			synchronized(channelGroup)
			{
				Channel[] channels = Arrays.copyOf(this.channels, this.channels.length + 1);
				channels[this.channels.length] = channel;
				this.channels = channels;
			}
			
			// Setup a listener to remove this channel when it is closed
			channel.closeFuture().addListener(r -> this.removeChannel(channel));
			
			connectionFuture.complete(null);
		}
	}
	
	public void removeChannel(Channel channel)
	{
		if(channelGroup.remove(channel))
		{
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
				
				this.channels = channels;
				
				if(channels.length < connections)
				{
					startConnectionToServer();
				}
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
	public void send(ByteMessage message)
	{
		Channel[] current = this.channels;
		if(current.length == 0)
		{
			// TODO: How do we handle cases where we have no connection?
			return;
		}
		
		current[0].writeAndFlush(message);
	}
}
