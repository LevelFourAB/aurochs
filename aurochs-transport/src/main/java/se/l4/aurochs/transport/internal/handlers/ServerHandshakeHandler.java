package se.l4.aurochs.transport.internal.handlers;

import java.util.UUID;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import se.l4.aurochs.transport.internal.TransportFunctions;
import se.l4.aurochs.transport.internal.TransportSession;
import se.l4.aurochs.transport.internal.handshake.Authenticate;
import se.l4.aurochs.transport.internal.handshake.BeginSession;
import se.l4.aurochs.transport.internal.handshake.Capabilities;
import se.l4.aurochs.transport.internal.handshake.Ok;
import se.l4.aurochs.transport.internal.handshake.Reject;
import se.l4.aurochs.transport.internal.handshake.SelectCapabilities;
import se.l4.aurochs.transport.internal.handshake.SessionStatus;

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
	
	private State state;
	
	public ServerHandshakeHandler(TransportFunctions functions)
	{
		this.functions = functions;
		
		state = State.WAITING_FOR_CAPS;
	}
	
	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
		throws Exception
	{
		Channel channel = e.getChannel();
		
		channel.write(new Capabilities("NONE"));
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
					
					functions.setupPipeline(session, channel);
				}
					
		}
	}
}
