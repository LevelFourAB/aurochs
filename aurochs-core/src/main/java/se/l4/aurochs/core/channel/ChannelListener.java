package se.l4.aurochs.core.channel;


public interface ChannelListener<T>
{
	void messageReceived(MessageEvent<T> event);
}
