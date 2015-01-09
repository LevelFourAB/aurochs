package se.l4.aurochs.cluster.internal.raft;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;

public class StaticNodes
	implements Nodes
{
	private final Collection<Node<Object>> nodes;

	@SafeVarargs
	public StaticNodes(Node<Object>... nodes)
	{
		this(Arrays.asList(nodes));
	}
	
	public StaticNodes(Collection<Node<Object>> nodes)
	{
		this.nodes = nodes;
	}
	
	@Override
	public void listen(Consumer<NodeEvent<Object>> consumer)
	{
		for(Node<Object> n : nodes)
		{
			consumer.accept(new NodeEvent<>(NodeEvent.Type.INITIAL, n));
		}
	}
}
