package se.l4.aurochs.core.spi;

import java.util.function.Predicate;

import se.l4.aurochs.core.channel.Channel;
import se.l4.aurochs.core.channel.ChannelListener;
import se.l4.aurochs.core.channel.MessageEvent;

/**
 * Implementation of {@link Channel} that filters another channel.
 * 
 * @author Andreas Holstenson
 *
 * @param <T>
 */
@SuppressWarnings({ "rawtypes", "unchecked" }) 
public class FilteredChannel<T>
	extends AbstractChannel<T>
{
	private final Channel channel;
	private final Predicate predicate;

	public FilteredChannel(Channel<?> channel, Predicate<?> predicate)
	{
		this.channel = channel;
		this.predicate = predicate;
		
		channel.addListener(new ChannelListener()
		{
			@Override
			public void messageReceived(MessageEvent event)
			{
				handleMessageReceived(event);
			}
		});
	}

	protected void handleMessageReceived(MessageEvent event)
	{
		if(predicate.test(event.getMessage()))
		{
			fireMessageReceived(event);
		}
	}

	@Override
	public void send(T message)
	{
		channel.send(message);
	}

	@Override
	public void close()
	{
		channel.close();
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "{channel=" + channel + ", predicate=" + predicate + "}";
	}
}
