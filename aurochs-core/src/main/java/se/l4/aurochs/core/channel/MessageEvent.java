package se.l4.aurochs.core.channel;

/**
 * Event for when a channel receives a message. The event originates from a
 * {@link #getChannel() channel} but may have a different 
 * {@link #getReturnPath() return path} that should be used to send answers
 * to the message.
 * 
 * @author Andreas Holstenson
 *
 * @param <T>
 */
public class MessageEvent<T>
{
	private final Channel<T> channel;
	private final Channel<Object> returnPath;
	private final T object;

	public MessageEvent(Channel<T> channel, Channel<?> returnPath, T object)
	{
		this.channel = channel;
		this.returnPath = (Channel) returnPath;
		this.object = object;
	}

	/**
	 * Get the channel where this message was received.
	 * 
	 * @return
	 */
	public Channel<T> getChannel()
	{
		return channel;
	}
	
	/**
	 * Get a channel that can be used to send a message back to the sender.
	 * 
	 * @return
	 */
	public Channel<Object> getReturnPath()
	{
		return returnPath;
	}

	/**
	 * Get the actual object received.
	 * 
	 * @return
	 */
	public T getMessage()
	{
		return object;
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[message=" + object + ", channel=" + channel + ", returnPath=" + returnPath + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((channel == null) ? 0 : channel.hashCode());
		result = prime * result + ((object == null) ? 0 : object.hashCode());
		result = prime * result
				+ ((returnPath == null) ? 0 : returnPath.hashCode());
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
		if(returnPath == null)
		{
			if(other.returnPath != null)
				return false;
		}
		else if(!returnPath.equals(other.returnPath))
			return false;
		return true;
	}
}
