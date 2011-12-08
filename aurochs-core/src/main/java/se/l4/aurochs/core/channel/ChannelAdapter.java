package se.l4.aurochs.core.channel;

/**
 * Adapter for {@link ChannelListener}.
 * 
 * @author Andreas Holstenson
 *
 * @param <T>
 */
public abstract class ChannelAdapter<T>
	implements ChannelListener<T>
{

	@Override
	public void messageReceived(MessageEvent<T> event)
	{
	}

}
