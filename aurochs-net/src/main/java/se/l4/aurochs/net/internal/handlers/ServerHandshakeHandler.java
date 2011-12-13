package se.l4.aurochs.net.internal.handlers;

import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;

import javax.net.ssl.SSLEngine;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.ssl.SslHandler;

import se.l4.aurochs.net.internal.TransportFunctions;
import se.l4.aurochs.net.internal.TransportSession;
import se.l4.aurochs.net.internal.handshake.Authenticate;
import se.l4.aurochs.net.internal.handshake.BeginSession;
import se.l4.aurochs.net.internal.handshake.Capabilities;
import se.l4.aurochs.net.internal.handshake.Ok;
import se.l4.aurochs.net.internal.handshake.Reject;
import se.l4.aurochs.net.internal.handshake.SelectCapabilities;
import se.l4.aurochs.net.internal.handshake.SessionStatus;

import com.google.inject.Provider;

/**
 * Handler for the handshake as seen by a server.
 * 
 * @author Andreas Holstenson
 *
 */
public class ServerHandshakeHandler
	extends SimpleChannelHandler
{
	private enum State
	{
		WAITING_FOR_CAPS,
		WAITING_FOR_AUTH,
		WAITING_FOR_BEGIN
	}

	private final TransportFunctions functions;
	private final ThreadPoolExecutor executor;
	private final Provider<SSLEngine> engines;
	
	private State state;
	
	public ServerHandshakeHandler(TransportFunctions functions, 
			ThreadPoolExecutor executor, 
			Provider<SSLEngine> engines)
	{
		this.functions = functions;
		this.executor = executor;
		this.engines = engines;
		
		state = State.WAITING_FOR_CAPS;
	}
	
	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
		throws Exception
	{
		Channel channel = e.getChannel();
		
		channel.write(new Capabilities(engines == null ? "NONE" : "TLS"));
	}
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
		throws Exception
	{
		Object msg = e.getMessage();
		Channel channel = e.getChannel();
		
		switch(state)
		{
			case WAITING_FOR_CAPS:
				if(msg instanceof SelectCapabilities)
				{
					SelectCapabilities caps = (SelectCapabilities) msg;
					if(caps.isSelected("TLS"))
					{
						SslHandler handler = new SslHandler(engines.get(), true);
						channel.getPipeline().addFirst("ssl", handler);
					}
					
					state = State.WAITING_FOR_AUTH;
					channel.write(new Ok());
				}
				else
				{
					channel.write(new Reject("Expected SELECT"));
					channel.disconnect();
				}
				break;
			case WAITING_FOR_AUTH:
				if(msg instanceof Authenticate)
				{
					Authenticate auth = (Authenticate) msg;
					if(auth.getMethod() == null)
					{
						// TODO: Correct SASL methods
						channel.write(new Reject("ANONYMOUS"));
					}
					else if(auth.getMethod().equals("ANONYMOUS"))
					{
						state = State.WAITING_FOR_BEGIN;
						channel.write(new Ok());
					}
					else
					{
						channel.write(new Reject("Unknown authentication"));
					}
				}
				else
				{
					channel.write(new Reject("Expected AUTH"));
					channel.disconnect();
				}
				break;
			case WAITING_FOR_BEGIN:
				if(msg instanceof BeginSession)
				{
					BeginSession begin = (BeginSession) msg;

					UUID uuid = UUID.randomUUID();
					String id = uuid.toString();
					
					TransportSession session = functions.createSession(channel, id);
					
					// Send the final handshake message
					channel.write(new SessionStatus(id));
					
					functions.setupPipeline(executor, session, channel);
				}
					
		}
	}
}
