package se.l4.aurochs.net.internal.handlers;

import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import javax.net.ssl.TrustManager;

import com.google.common.base.Throwables;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.ssl.SslHandler;
import se.l4.aurochs.channels.ByteMessage;
import se.l4.aurochs.net.ConnectionException;
import se.l4.aurochs.net.ServerConnection.TLSMode;
import se.l4.aurochs.net.internal.SslHelper;
import se.l4.aurochs.net.internal.TransportFunctions;
import se.l4.aurochs.net.internal.handshake.Authenticate;
import se.l4.aurochs.net.internal.handshake.BeginSession;
import se.l4.aurochs.net.internal.handshake.Capabilities;
import se.l4.aurochs.net.internal.handshake.Ok;
import se.l4.aurochs.net.internal.handshake.Reject;
import se.l4.aurochs.net.internal.handshake.SelectCapabilities;
import se.l4.aurochs.net.internal.handshake.SessionStatus;

public class ClientHandshakeHandler
	extends ChannelInboundHandlerAdapter
{
	private enum State
	{
		WAITING_FOR_CAPS,
		WAITING_FOR_SELECT_OK,
		WAITING_FOR_AUTH_REJECT, 
		WAITING_FOR_AUTH_OK, 
		WAITING_FOR_SESSION
	}

	private final TransportFunctions functions;
	private final Executor executor;
	
	private final TLSMode tlsMode;
	private final TrustManager trustManager;
	
	private final CompletableFuture<Channel> future;
	private final Consumer<ByteMessage> messageReceiver;
	
	private State state;
	private boolean tls;
	
	public ClientHandshakeHandler(TransportFunctions functions,
			Executor executor,
			TLSMode tlsMode, 
			TrustManager trustManager,
			Consumer<ByteMessage> messageReceiver,
			CompletableFuture<Channel> future)
	{
		this.functions = functions;
		this.executor = executor;
		
		this.tlsMode = tlsMode;
		this.trustManager = trustManager;
		this.messageReceiver = messageReceiver;
		this.future = future;
		
		state = State.WAITING_FOR_CAPS;
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
		throws Exception
	{
		Channel channel = ctx.channel();
		
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
					if(tls)
					{
						// TLS was selected, add the handler
						SslHandler handler = new SslHandler(SslHelper.createClientEngine(trustManager), false);
						channel.pipeline().addFirst("ssl", handler);
					}
					
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
					ConnectionException ex = new ConnectionException("Authentication failed");
					future.completeExceptionally(ex);
				}
				break;
			case WAITING_FOR_SESSION:
				if(msg instanceof SessionStatus)
				{
					// TODO: Send back the session id
					functions.setupPipeline(executor, messageReceiver, channel);

					future.complete(channel);
				}
				else
				{
					raiseError(ctx);
				}
				break;
		}
		
		ctx.flush();
	}
	
	private void raiseError(ChannelHandlerContext ctx)
	{
		ConnectionException ex = new ConnectionException("Connection to server did not succeed");
		future.completeExceptionally(ex);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
		throws Exception
	{
		Throwable root = Throwables.getRootCause(cause);
		
		if(root instanceof CertificateException)
		{
			future.completeExceptionally(new ConnectionException("Connection to server did not succeed; Certificate was not trusted", root));
		}
		else
		{
			future.completeExceptionally(new ConnectionException("Connection to server did not succeed", cause.getCause()));
		}
	}

	private SelectCapabilities selectCapabilities(Capabilities caps)
	{
		List<String> selected = new ArrayList<String>();
		switch(tlsMode)
		{
			case AUTOMATIC:
				if(caps.has("TLS"))
				{
					tls = true;
					selected.add("TLS");
				}
				break;
			case FORCE:
				if(caps.has("TLS"))
				{
					tls = true;
					selected.add("TLS");
				}
				else
				{
					future.completeExceptionally(new ConnectionException("Server does not support TLS"));
				}
				break;
			case NEVER:
				// Do nothing
		}
		
		return new SelectCapabilities(selected);
	}
}
