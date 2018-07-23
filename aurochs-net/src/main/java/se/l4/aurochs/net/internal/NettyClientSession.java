package se.l4.aurochs.net.internal;

import java.util.concurrent.CompletableFuture;

import com.google.inject.Injector;

import se.l4.aurochs.channels.ByteMessage;
import se.l4.aurochs.channels.Channel;
import se.l4.aurochs.net.RemoteSession;
import se.l4.aurochs.sessions.AbstractSession;

public class NettyClientSession
	extends AbstractSession
	implements RemoteSession
{
	private final Channel<Object> objectChannel;
	private final NettyClientChannel rawChannel;

	public NettyClientSession(Injector injector,
			NettyClientChannel rawChannel,
			Channel<Object> objectChannel)
	{
		super(injector);
		
		this.rawChannel = rawChannel;
		this.objectChannel = objectChannel;
	}
	
	@Override
	public boolean isConnected()
	{
		return rawChannel.isConnected();
	}
	
	@Override
	public CompletableFuture<Void> connectionFuture()
	{
		return rawChannel.getConnectionFuture();
	}

	@Override
	public Channel<ByteMessage> getRawChannel()
	{
		return rawChannel;
	}

	@Override
	public Channel<Object> getObjectChannel()
	{
		return objectChannel;
	}

}
