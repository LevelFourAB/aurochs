package se.l4.aurochs.core.channel;

import se.l4.aurochs.core.spi.AbstractChannel;

public class LocalChannel<T>
{
	private final ActualChannel<T> incoming;
	private final ActualChannel<T> outgoing;

	public LocalChannel()
	{
		incoming = new ActualChannel<>();
		outgoing = new ActualChannel<>();
		
		incoming.other = outgoing;
		outgoing.other = incoming;
	}
	
	public static <T> LocalChannel<T> create()
	{
		return new LocalChannel<>();
	}
	
	public Channel<T> getIncoming()
	{
		return incoming;
	}
	
	public Channel<T> getOutgoing()
	{
		return outgoing;
	}
	
	private static class ActualChannel<T>
		extends AbstractChannel<T>
	{
		private ActualChannel<T> other;
		
		@Override
		public void send(T message)
		{
			other.fireMessageReceived(new MessageEvent<T>(other, this, message));
		}
		
		@Override
		public void close()
		{
		}
	}
}
