package se.l4.aurochs.cluster.nodes;

import java.util.function.Consumer;

import se.l4.aurochs.cluster.internal.TransformedNodes;
import se.l4.aurochs.core.channel.ChannelCodec;
import se.l4.aurochs.core.events.EventHandle;

public interface Nodes<D>
{
	/**
	 * Start listening for changes to nodes. This listener will receive all the
	 * current nodes with type {@link NodeEvent.Type#INITIAL} before the
	 * method returns.
	 * 
	 * @param consumer
	 * @return
	 */
	EventHandle listen(Consumer<NodeEvent<D>> consumer);
	
	/**
	 * Get a node based on its identifier.
	 * 
	 * @param id
	 * @return
	 */
	Node<D> get(String id);
	
	default <T> Nodes<T> transform(ChannelCodec<D, T> codec)
	{
		return new TransformedNodes<>(this, codec);
	}
}
