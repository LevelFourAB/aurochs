package se.l4.aurochs.cluster.internal;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;

import se.l4.aurochs.cluster.nodes.Node;
import se.l4.aurochs.cluster.nodes.NodeEvent;
import se.l4.aurochs.cluster.nodes.Nodes;
import se.l4.aurochs.core.events.EventHandle;

public class StaticNodes<T>
	implements Nodes<T>
{
	private final Collection<Node<T>> nodes;

	@SafeVarargs
	public StaticNodes(Node<T>... nodes)
	{
		this(Arrays.asList(nodes));
	}
	
	public StaticNodes(Collection<Node<T>> nodes)
	{
		this.nodes = nodes;
	}
	
	@Override
	public Node<T> get(String id)
	{
		for(Node<T> node : nodes)
		{
			if(node.getId().equals(id)) return node;
		}
		
		return null;
	}
	
	@Override
	public EventHandle listen(Consumer<NodeEvent<T>> consumer)
	{
		for(Node<T> n : nodes)
		{
			consumer.accept(new NodeEvent<>(NodeEvent.Type.INITIAL, n));
		}
		
		return EventHandle.noop();
	}
}
