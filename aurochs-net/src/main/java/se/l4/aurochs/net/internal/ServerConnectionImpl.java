package se.l4.aurochs.net.internal;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.net.URI;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.net.ssl.TrustManager;

import se.l4.aurochs.core.hosts.Hosts;
import se.l4.aurochs.net.RemoteSession;
import se.l4.aurochs.net.ServerConnection;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Injector;

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
	private final Injector injector;
	
	private Hosts hosts;
	
	private TLSMode tlsMode;
	private TrustManager trustManager;

	@Inject
	public ServerConnectionImpl(Injector injector, ClientTransportFunctions functions)
	{
		this.injector = injector;
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
			.setDaemon(true)
			.build()
		);
		
		EventLoopGroup group = new NioEventLoopGroup();
		NettyClientChannel channel = new NettyClientChannel(group, functions, executor, tlsMode, trustManager, hosts);
		
		channel.start();
		
		return new NettyClientSession(injector, channel, functions.createObjectChannel(channel));
	}
	
	public Future<ConnectionResult> connectInternal()
	{
		return null;
	}
}
