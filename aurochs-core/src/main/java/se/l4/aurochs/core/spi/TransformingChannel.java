package se.l4.aurochs.core.spi;

import java.util.function.Function;

import se.l4.aurochs.core.channel.Channel;
import se.l4.aurochs.core.channel.MessageEvent;

public class TransformingChannel<O, T>
	extends AbstractChannel<T>
{
	private final Channel<O> channel;
	private final Function<O, T> to;
	private final Function<T, O> from;

	public TransformingChannel(Channel<O> channel, Function<O, T> to, Function<T, O> from)
	{
		this.channel = channel;
		this.to = to;
		this.from = from;
		
		channel.addListener((event) -> fireMessageReceived(
			new MessageEvent<T>(this, event.getReturnPath(), to.apply(event.getMessage())))
		);
	}
	
	@Override
	public void close()
	{
		channel.close();
	}
	
	@Override
	public void send(T message)
	{
		channel.send(from.apply(message));
	}
}
