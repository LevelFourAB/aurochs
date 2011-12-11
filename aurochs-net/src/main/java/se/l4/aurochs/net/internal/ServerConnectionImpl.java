package se.l4.aurochs.net.internal;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;

import se.l4.aurochs.core.Session;
import se.l4.aurochs.net.ConnectionException;
import se.l4.aurochs.net.ServerConnection;
import se.l4.aurochs.net.internal.handlers.ClientHandshakeHandler;
import se.l4.aurochs.net.internal.handlers.HandshakeDecoder;
import se.l4.aurochs.net.internal.handlers.HandshakeEncoder;

import com.google.common.util.concurrent.SettableFuture;
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
	private InetSocketAddress address;

	@Inject
	public ServerConnectionImpl(ClientTransportFunctions functions)
	{
		this.functions = functions;
	}
	
	@Override
	public ServerConnection setAddress(String hostname, int port)
	{
		this.address = new InetSocketAddress(hostname, port);
		
		return this;
	}
	
	public Session connect()
		throws ConnectionException
	{
		if(address == null)
		{
			throw new IllegalArgumentException("No address specified");
		}
		
		ClientBootstrap bootstrap = new ClientBootstrap(
			new NioClientSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool()
			)
		);
		
		// Set up the event pipeline factory.
		bootstrap.setPipelineFactory(new ChannelPipelineFactory()
		{
			public ChannelPipeline getPipeline() throws Exception
			{
				ChannelPipeline pipeline = Channels.pipeline();
				
				pipeline.addLast("handshakeDecoderByLine", new DelimiterBasedFrameDecoder(4096, Delimiters.lineDelimiter()));
				pipeline.addLast("handshakeDecoder", new HandshakeDecoder());
				pipeline.addLast("handshakeEncoder", new HandshakeEncoder());
				
				pipeline.addLast("handshake", new ClientHandshakeHandler(functions));
				
				return pipeline;
			}
		});
		
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("keepAlive", true);
		
		ChannelFuture cf = bootstrap.connect(address);
		
		try
		{
			cf.await(60, TimeUnit.SECONDS);
		}
		catch(InterruptedException e)
		{
			throw new RuntimeException("Unable to connect to server");
		}
		
		if(! cf.isSuccess())
		{
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
}
