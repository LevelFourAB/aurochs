package se.l4.aurochs.channels;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class CombiningChannel<T>
	extends AbstractChannel<T>
{
	private static final Channel[] EMPTY = new Channel[0];
	
	private final Object lock;
	private final MessageListener<T> listener;
	
	private volatile Channel[] channels;
	
	public CombiningChannel()
	{
		lock = new Object();
		listener = e -> fireMessageReceived(new MessageEvent<T>(this, this, e.getMessage()));
		
		channels = EMPTY;
	}
	
	@Override
	public void send(T message)
	{
		Channel[] channels = this.channels;
		if(channels.length == 0) return;
		
		int idx = ThreadLocalRandom.current().nextInt(channels.length);
		channels[idx].send(message);
	}
	
	@Override
	public void close()
	{
		Channel[] channels = this.channels;
		for(Channel c : channels)
		{
			c.close();
		}
	}
	
	public void addChannel(Channel<T> channel)
	{
		synchronized(lock)
		{
			Channel[] channels = Arrays.copyOf(this.channels, this.channels.length + 1);
			channels[this.channels.length] = channel;
			channel.addMessageListener(listener);
			this.channels = channels;
		}
	}
	
	public void removeChannel(Channel<T> channel)
	{
		synchronized(lock)
		{
			Channel[] channels = this.channels;
			int index = -1;
			for(int i=0, n=channels.length; i<n; i++)
			{
				if(channels[i] == channel)
				{
					index = i;
					break;
				}
			}
			
			if(index == -1)
			{
				// Nothing to do, no such listener
				return;
			}
			
			channel.removeMessageListener(listener);
			
			int length = channels.length;
			Channel[] result = new Channel[length - 1];
			System.arraycopy(channels, 0, result, 0, index);
			
			if(index < length - 1)
			{
				System.arraycopy(channels, index + 1, result, index, length - index - 1);
			}
			
			this.channels = result;
		}
	}
}
