package se.l4.aurochs.net.internal;

import java.io.IOException;

import org.jboss.netty.channel.Channel;

import se.l4.aurochs.core.channel.MessageEvent;
import se.l4.aurochs.core.io.ByteMessage;
import se.l4.aurochs.core.io.Bytes;
import se.l4.aurochs.core.io.DefaultByteMessage;
import se.l4.aurochs.core.spi.AbstractChannel;
import se.l4.aurochs.core.spi.AbstractSession;
import se.l4.aurochs.serialization.standard.CompactDynamicSerializer;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.inject.Injector;

public class TransportSession
	extends AbstractSession
{
	private final Channel nettyChannel;
	private final ByteMessageChannel rawChannel;
	private se.l4.aurochs.core.channel.Channel<Object> objectChannel;

	public TransportSession(Injector injector, Channel nettyChannel, CompactDynamicSerializer serializer)
	{
		super(injector);

		this.nettyChannel = nettyChannel;
		rawChannel = new ByteMessageChannel(nettyChannel);
		
		objectChannel = rawChannel
			.filter(ByteMessage.tag(1))
			.transform(
				o -> {
					try {
						return serializer.fromBytes(o.getData().toByteArray());
					} catch(IOException e) {
						throw Throwables.propagate(e);
					}
				},
				o -> new DefaultByteMessage(1, Bytes.create(serializer.toBytes(o)))
			);
	}
	
	@Override
	public se.l4.aurochs.core.channel.Channel<ByteMessage> getRawChannel()
	{
		return rawChannel;
	}
	
	@Override
	public se.l4.aurochs.core.channel.Channel<Object> getObjectChannel()
	{
		return objectChannel;
	}

	@Override
	public String toString()
	{
		return Objects.toStringHelper(this)
			.add("connection", nettyChannel.getRemoteAddress())
			.toString();
	}
	
	public void receive(ByteMessage msg)
	{
		rawChannel.fireMessageReceived(new MessageEvent<ByteMessage>(rawChannel, rawChannel, msg));
	}
	
	private static class ByteMessageChannel
		extends AbstractChannel<ByteMessage>
	{
		private final Channel nettyChannel;

		public ByteMessageChannel(Channel nettyChannel)
		{
			this.nettyChannel = nettyChannel;
		}
		
		@Override
		public void fireMessageReceived(MessageEvent<? extends ByteMessage> event)
		{
			super.fireMessageReceived(event);
		}
		
		@Override
		public void send(ByteMessage message)
		{
			nettyChannel.write(message);
		}
		
		@Override
		public void close()
		{
			nettyChannel.close();
		}
	}

}
