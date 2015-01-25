package se.l4.aurochs.cluster.internal;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

import se.l4.aurochs.cluster.nodes.Node;
import se.l4.aurochs.cluster.nodes.NodeEvent;
import se.l4.aurochs.cluster.nodes.NodeSet;
import se.l4.aurochs.core.events.EventHandle;
import se.l4.aurochs.core.events.EventHelper;

public class MutableNodes<D>
	implements NodeSet<D>
{
	private final EventHelper<Consumer<NodeEvent<D>>> events;
	private final Set<Node<D>> nodes;
	
	public MutableNodes()
	{
		events = new EventHelper<>();
		nodes = new CopyOnWriteArraySet<>();
	}
	
	public void addNode(Node<D> node)
	{
		if(nodes.add(node))
		{
			NodeEvent<D> event = new NodeEvent<>(NodeEvent.Type.ADDED, node);
			events.forEach(in -> in.accept(event));
		}
	}
	
	public void removeNode(Node<D> node)
	{
		if(nodes.remove(node))
		{
			NodeEvent<D> event = new NodeEvent<>(NodeEvent.Type.REMOVED, node);
			events.forEach(in -> in.accept(event));
		}
	}
	
	public Collection<Node<D>> list()
	{
		return nodes;
	}
	
	@Override
	public Node<D> get(String id)
	{
		for(Node<D> node : nodes)
		{
			if(node.getId().equals(id)) return node;
		}
		
		return null;
	}
	
	@Override
	public EventHandle listen(Consumer<NodeEvent<D>> consumer)
	{
		for(Node<D> node : nodes)
		{
			consumer.accept(new NodeEvent<>(NodeEvent.Type.INITIAL, node));
		}
		return events.listen(consumer);
	}
}
