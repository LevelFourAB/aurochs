package se.l4.aurochs.channels;

import java.util.function.Predicate;

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
		
		channel.addMessageListener(this::handleMessageReceived);
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
