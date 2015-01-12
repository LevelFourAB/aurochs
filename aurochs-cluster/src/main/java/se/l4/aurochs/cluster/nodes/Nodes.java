package se.l4.aurochs.cluster.nodes;

import java.util.function.Consumer;

import se.l4.aurochs.cluster.internal.TransformedNodes;
import se.l4.aurochs.core.channel.ChannelCodec;
import se.l4.aurochs.core.events.EventHandle;

public interface Nodes<D>
{
	EventHandle listen(Consumer<NodeEvent<D>> consumer);
	
	default <T> Nodes<T> transform(ChannelCodec<D, T> codec)
	{
		return new TransformedNodes<>(this, codec);
	}
}
