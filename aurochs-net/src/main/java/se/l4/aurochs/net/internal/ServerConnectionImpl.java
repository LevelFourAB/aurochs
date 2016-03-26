package se.l4.aurochs.net.internal;

import java.net.URI;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import javax.net.ssl.TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Injector;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import se.l4.aurochs.core.channel.Channel;
import se.l4.aurochs.core.hosts.Hosts;
import se.l4.aurochs.net.RemoteSession;
import se.l4.aurochs.net.ServerConnection;
import se.l4.commons.io.ByteMessage;

/**
 * Implementation of {@link ServerConnection}.
 * 
 * @author Andreas Holstenson
 *
 */
public class ServerConnectionImpl
	implements ServerConnection
{
	private static final Logger log = LoggerFactory.getLogger(ServerConnection.class);
	
	private final TransportFunctions functions;
	private final Injector injector;
	
	private Hosts hosts;
	
	private TLSMode tlsMode;
	private TrustManager trustManager;

	private Consumer<Channel<ByteMessage>> initializer;

	private int minConnections;

	@Inject
	public ServerConnectionImpl(Injector injector, TransportFunctions functions)
	{
		this.injector = injector;
		this.functions = functions;
		
		minConnections = 1;
		
		tlsMode = TLSMode.AUTOMATIC;
		trustManager = SslHelper.TRUSTING;
		
		initializer = (c) -> {};
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
	public ServerConnection setHosts(Hosts hosts)
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
	public ServerConnection setChannelInitializer(Consumer<Channel<ByteMessage>> initializer)
	{
		this.initializer = initializer;
		return this;
	}
	
	@Override
	public ServerConnection setMinConnections(int minConnections)
	{
		this.minConnections = minConnections;
		return this;
	}
	
	@Override
	public RemoteSession connect()
	{
		if(hosts == null)
		{
			throw new IllegalArgumentException("No hosts specified");
		}
		
		Collection<URI> hosts = this.hosts.list();
		if(hosts.isEmpty())
		{
			throw new IllegalArgumentException("No hosts specified in set");
		}
		
		ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
			.setNameFormat("aurochs-client-%s " + this.hosts)
			.setUncaughtExceptionHandler((t, e) -> log.warn("Uncaught exception; " + e.getMessage(), e))
			.setDaemon(true)
			.build()
		);
		
		EventLoopGroup group = new NioEventLoopGroup();
		NettyClientChannel channel = new NettyClientChannel(group, functions, executor, tlsMode, trustManager, hosts, minConnections, initializer);
		
		channel.start();
		
		return new NettyClientSession(injector, channel, functions.createObjectChannel(channel));
	}
	
	public Future<ConnectionResult> connectInternal()
	{
		return null;
	}
}
