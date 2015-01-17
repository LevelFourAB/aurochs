package se.l4.aurochs.cluster.internal;

import java.util.function.Consumer;

import se.l4.aurochs.cluster.nodes.Node;
import se.l4.aurochs.cluster.nodes.NodeEvent;
import se.l4.aurochs.cluster.nodes.Nodes;
import se.l4.aurochs.core.channel.Channel;
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
					new Node<>(e.getNode().getId(), null, null)
				));
			}
			else
			{
				Channel<D> incoming;
				Channel<D> outgoing;
				if(e.getNode().incoming() == e.getNode().outgoing())
				{
					incoming = outgoing = e.getNode().incoming().transform(codec);
				}
				else
				{
					incoming = e.getNode().incoming().transform(codec);
					outgoing = e.getNode().outgoing().transform(codec);
				}
				
				consumer.accept(new NodeEvent<>(
					e.getType(),
					new Node<>(e.getNode().getId(), incoming, outgoing)
				));
			}
		});
	}
}
