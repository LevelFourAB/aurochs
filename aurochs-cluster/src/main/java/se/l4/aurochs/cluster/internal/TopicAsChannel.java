package se.l4.aurochs.cluster.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import se.l4.aurochs.core.channel.Channel;
import se.l4.aurochs.core.channel.ChannelListener;
import se.l4.aurochs.core.channel.MessageEvent;
import se.l4.aurochs.core.spi.AbstractChannel;
import se.l4.aurochs.serialization.Serializer;

import com.hazelcast.core.ITopic;
import com.hazelcast.core.MessageListener;

/**
 * Wrapper for {@link ITopic} that implements {@link Channel}.
 * 
 * @author Andreas Holstenson
 *
 * @param <T>
 */
public class TopicAsChannel<T>
	extends AbstractChannel<T>
{
	private final ITopic<byte[]> topic;
	private final Function<T, byte[]> toBytes;
	private final Function<byte[], T> fromBytes;
	private final Map<Object, String> registeredListeners;

	public TopicAsChannel(ITopic<byte[]> topic, final Serializer<T> serializer)
	{
		this.topic = topic;
		
		registeredListeners = new ConcurrentHashMap<Object, String>();
		
		toBytes = serializer.toBytes();
		fromBytes = serializer.fromBytes();
	}

	@Override
	public void addListener(ChannelListener<T> listener)
	{
		MessageListener<byte[]> ml = (msg) -> 
			listener.messageReceived(
				new MessageEvent<>(TopicAsChannel.this, TopicAsChannel.this, fromBytes.apply(msg.getMessageObject()))
			);
		
		
		String id = topic.addMessageListener(ml);
		registeredListeners.put(listener, id);
	}

	@Override
	public void removeListener(ChannelListener<T> listener)
	{
		String id = registeredListeners.get(listener);
		if(id != null)
		{
			topic.removeMessageListener(id);
		}
	}
	
	@Override
	public void send(T message)
	{
		topic.publish(toBytes.apply(message));
	}
	
	@Override
	public void close()
	{
	}
}
