package se.l4.aurochs.net.internal;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.TrustManager;

import se.l4.aurochs.core.Session;
import se.l4.aurochs.net.ConnectionException;
import se.l4.aurochs.net.ServerConnection;
import se.l4.aurochs.net.hosts.HostSet;
import se.l4.aurochs.net.hosts.Hosts;
import se.l4.aurochs.net.internal.handlers.ClientHandshakeHandler;
import se.l4.aurochs.net.internal.handlers.HandshakeDecoder;
import se.l4.aurochs.net.internal.handlers.HandshakeEncoder;

import com.google.common.util.concurrent.SettableFuture;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;

/**
 * Implementation of {@link ServerConnection}.
 * 
 * @author Andreas Holstenson
 *
 */
public class ServerConnectionImpl
	implements ServerConnection
{
	private final ClientTransportFunctions functions;
	
	private HostSet hosts;
	
	private TLSMode tlsMode;
	private TrustManager trustManager;
	

	@Inject
	public ServerConnectionImpl(ClientTransportFunctions functions)
	{
		this.functions = functions;
		
		tlsMode = TLSMode.AUTOMATIC;
		trustManager = SslHelper.TRUSTING;
	}
	
	@Override
	public ServerConnection setHost(String uri)
	{
		return setHosts(Hosts.create(uri));
	}
	
	@Override
	public ServerConnection setHost(URI uri)
	{
		return setHosts(Hosts.create(uri));
	}
	
	@Override
	public ServerConnection setHosts(HostSet hosts)
	{
		this.hosts = hosts;
		
		return this;
	}
	
	@Override
	public ServerConnection setTLS(TLSMode mode)
	{
		if(mode == null)
		{
			throw new IllegalArgumentException("TLS mode can not be null");
		}
		
		this.tlsMode = mode;
		
		return this;
	}
	
	@Override
	public ServerConnection setTrustManager(TrustManager trustManager)
	{
		if(trustManager == null)
		{
			throw new IllegalArgumentException("A trust manager is required");
		}
		
		this.trustManager = trustManager;
		
		return this;
	}
	
	@Override
	public Session connect()
		throws ConnectionException
	{
		if(hosts == null)
		{
			throw new IllegalArgumentException("No hosts specified");
		}
		
		Set<URI> hosts = this.hosts.list();
		if(hosts.isEmpty())
		{
			throw new IllegalArgumentException("No hosts specified in set");
		}
		
		final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
			.setNameFormat("aurochs-client-%s " + this.hosts)
			.setDaemon(true)
			.build()
		);
		
		EventLoopGroup group = new NioEventLoopGroup();
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
					
					pipeline.addLast("handshake", new ClientHandshakeHandler(functions, executor, tlsMode, trustManager));
				}
			});
		
		URI uri = hosts.iterator().next();
		InetSocketAddress address = new InetSocketAddress(
			uri.getHost(), 
			uri.getPort() > 0 ? uri.getPort() : 7400
		);
		ChannelFuture cf = bootstrap.connect(address);
		
		try
		{
			cf.await(60, TimeUnit.SECONDS);
		}
		catch(InterruptedException e)
		{
			group.shutdownGracefully();
			throw new RuntimeException("Unable to connect to server");
		}
		
		if(! cf.isSuccess())
		{
			group.shutdownGracefully();
			throw new RuntimeException("Unable to connect to server");
		}
		
		SettableFuture<ConnectionResult> future = functions.getFuture();
		try
		{
			ConnectionResult result = future.get(30, TimeUnit.SECONDS);
			
			if(result.isError())
			{
				throw new ConnectionException(result.getException().getMessage(), result.getException());
			}
			
			return result.getSession();
		}
		catch(InterruptedException e)
		{
			throw new ConnectionException("Connection to server timed out");
		}
		catch(TimeoutException e)
		{
			throw new ConnectionException("Connection to server timed out");
		}
		catch(ExecutionException e)
		{
			throw new ConnectionException("Connection to server timed out");
		}
	}
	
	@Override
	public Session connectAsync()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	public Future<ConnectionResult> connectInternal()
	{
		return null;
	}
}
