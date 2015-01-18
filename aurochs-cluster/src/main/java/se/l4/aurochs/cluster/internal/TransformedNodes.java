package se.l4.aurochs.cluster.internal;

import java.util.function.Consumer;

import se.l4.aurochs.cluster.nodes.Node;
import se.l4.aurochs.cluster.nodes.NodeEvent;
import se.l4.aurochs.cluster.nodes.Nodes;
import se.l4.aurochs.core.channel.ChannelCodec;
import se.l4.aurochs.core.events.EventHandle;


public class TransformedNodes<O, D>
	implements Nodes<D>
{
	private final Nodes<O> nodes;
	private final ChannelCodec<O, D> codec;

	public TransformedNodes(Nodes<O> nodes, ChannelCodec<O, D> codec)
	{
		this.nodes = nodes;
		this.codec = codec;
	}
	
	@Override
	public Node<D> get(String id)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public EventHandle listen(Consumer<NodeEvent<D>> consumer)
	{
		return nodes.listen(e -> {
			if(e.getType() == NodeEvent.Type.REMOVED)
			{
				consumer.accept(new NodeEvent<>(
					NodeEvent.Type.REMOVED,
					new Node<>(e.getNode().getId(), null, null, null, null)
				));
			}
			else
			{
				consumer.accept(new NodeEvent<>(
					e.getType(),
					e.getNode().transform(codec)
				));
			}
		});
	}
}
