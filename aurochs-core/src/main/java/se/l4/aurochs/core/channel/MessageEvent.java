package se.l4.aurochs.core.channel;

/**
 * Event for when a channel receives a message.
 * 
 * @author Andreas Holstenson
 *
 * @param <T>
 */
public class MessageEvent<T>
{
	private final Channel<T> channel;
	private final T object;

	public MessageEvent(Channel<T> channel, T object)
	{
		this.channel = channel;
		this.object = object;
	}

	public Channel<T> getChannel()
	{
		return channel;
	}
	
	public T getObject()
	{
		return object;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((channel == null) ? 0 : channel.hashCode());
		result = prime * result + ((object == null) ? 0 : object.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		MessageEvent other = (MessageEvent) obj;
		if(channel == null)
		{
			if(other.channel != null)
				return false;
		}
		else if(!channel.equals(other.channel))
			return false;
		if(object == null)
		{
			if(other.object != null)
				return false;
		}
		else if(!object.equals(other.object))
			return false;
		return true;
	}
}
