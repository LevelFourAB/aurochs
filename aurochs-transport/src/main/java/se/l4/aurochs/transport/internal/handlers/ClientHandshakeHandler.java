package se.l4.aurochs.transport.internal.handlers;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import se.l4.aurochs.transport.ConnectionException;
import se.l4.aurochs.transport.internal.ClientTransportFunctions;
import se.l4.aurochs.transport.internal.TransportSession;
import se.l4.aurochs.transport.internal.handshake.Authenticate;
import se.l4.aurochs.transport.internal.handshake.BeginSession;
import se.l4.aurochs.transport.internal.handshake.Capabilities;
import se.l4.aurochs.transport.internal.handshake.Ok;
import se.l4.aurochs.transport.internal.handshake.Reject;
import se.l4.aurochs.transport.internal.handshake.SelectCapabilities;
import se.l4.aurochs.transport.internal.handshake.SessionStatus;

public class ClientHandshakeHandler
	extends SimpleChannelHandler
{
	private enum State
	{
		WAITING_FOR_CAPS,
		WAITING_FOR_SELECT_OK,
		WAITING_FOR_AUTH_REJECT, 
		WAITING_FOR_AUTH_OK, 
		WAITING_FOR_SESSION
	}

	private final ClientTransportFunctions functions;
	
	private State state;
	
	public ClientHandshakeHandler(ClientTransportFunctions functions)
	{
		this.functions = functions;
		state = State.WAITING_FOR_CAPS;
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
				if(msg instanceof Capabilities)
				{
					SelectCapabilities selected = selectCapabilities((Capabilities) msg);
					state = State.WAITING_FOR_SELECT_OK;
					channel.write(selected);
				}
				else
				{
					raiseError(ctx);
				}
				break;
			case WAITING_FOR_SELECT_OK:
				if(msg instanceof Ok)
				{
					state = State.WAITING_FOR_AUTH_REJECT;
					channel.write(new Authenticate());
				}
				else if(msg instanceof Reject)
				{
					raiseError(ctx);
				}
				else
				{
					raiseError(ctx);
				}
				break;
			case WAITING_FOR_AUTH_REJECT:
				if(msg instanceof Reject)
				{
					String[] possible = ((Reject) msg).getMessage().split(" ");
					
					// TODO: Actually select a suitable method
					
					state = State.WAITING_FOR_AUTH_OK;
					channel.write(new Authenticate("ANONYMOUS", new byte[0]));
				}
				else
				{
					raiseError(ctx);
				}
				break;
			case WAITING_FOR_AUTH_OK:
				if(msg instanceof Ok)
				{
					state =  State.WAITING_FOR_SESSION;
					channel.write(new BeginSession(null));
				}
				else
				{
					functions.raiseConnectionError(new ConnectionException("Authentication failed"));
				}
				break;
			case WAITING_FOR_SESSION:
				if(msg instanceof SessionStatus)
				{
					// TODO: Send back the session
					TransportSession session = functions.createSession(channel, ((SessionStatus) msg).getId());
					functions.setupPipeline(session, channel);
				}
				else
				{
					raiseError(ctx);
				}
				break;
		}
	}
	
	private void raiseError(ChannelHandlerContext ctx)
	{
		functions.raiseConnectionError(new ConnectionException("Connection to server did not succeed"));
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
		throws Exception
	{
		functions.raiseConnectionError(new ConnectionException("Connection to server did not succeed"));
	}

	private SelectCapabilities selectCapabilities(Capabilities caps)
	{
		return new SelectCapabilities();
	}
}
