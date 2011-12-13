package se.l4.aurochs.net.internal;

import se.l4.aurochs.core.channel.MessageEvent;
import se.l4.aurochs.core.spi.AbstractSession;

import com.google.common.base.Objects;
import com.google.inject.Injector;

public class TransportSession
	extends AbstractSession
{
	private final org.jboss.netty.channel.Channel nettyChannel;

	public TransportSession(Injector injector, org.jboss.netty.channel.Channel nettyChannel)
	{
		super(injector);
		
		this.nettyChannel = nettyChannel;
	}

	@Override
	public void send(Object message)
	{
		nettyChannel.write(message);
	}

	@Override
	public void fireMessageReceived(MessageEvent event)
	{
		super.fireMessageReceived(event);
	}
	
	@Override
	public String toString()
	{
		return Objects.toStringHelper(this)
			.add("connection", nettyChannel.getRemoteAddress())
			.toString();
	}
}
