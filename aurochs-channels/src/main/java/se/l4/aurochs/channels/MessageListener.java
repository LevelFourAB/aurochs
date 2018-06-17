package se.l4.aurochs.channels;

/**
 * Listener that is notified when a message is received in a {@link Channel}.
 */
@FunctionalInterface
public interface MessageListener<T>
{
	void messageReceived(MessageEvent<T> event);
}
