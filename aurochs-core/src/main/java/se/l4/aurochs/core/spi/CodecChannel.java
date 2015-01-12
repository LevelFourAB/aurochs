package se.l4.aurochs.core.spi;

import se.l4.aurochs.core.channel.Channel;
import se.l4.aurochs.core.channel.ChannelCodec;
import se.l4.aurochs.core.channel.MessageEvent;

/**
 * A channel that uses a {@link ChannelCodec} to filter and transform content.
 * 
 * @author Andreas Holstenson
 *
 * @param <O>
 * @param <T>
 */
public class CodecChannel<O, T>
	extends AbstractChannel<T>
{
	private final ChannelCodec<O, T> codec;
	private final Channel<O> channel;

	public CodecChannel(Channel<O> channel, ChannelCodec<O, T> codec)
	{
		this.channel = channel;
		this.codec = codec;
		
		channel.addListener((event) -> {
			O msg = event.getMessage();
			if(codec.accepts(msg))
			{
				fireMessageReceived(
					new MessageEvent<T>(this, event.getReturnPath(), codec.fromSource(msg))
				);
			}
		});
	}
	
	@Override
	public void close()
	{
		channel.close();
	}
	
	@Override
	public void send(T message)
	{
		channel.send(codec.toSource(message));
	}
}
