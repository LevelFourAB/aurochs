package se.l4.aurochs.cluster.internal;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import se.l4.aurochs.cluster.nodes.Node;
import se.l4.aurochs.cluster.nodes.NodeState;
import se.l4.aurochs.cluster.nodes.NodeStateEvent;
import se.l4.aurochs.cluster.nodes.NodeStates;
import se.l4.aurochs.core.events.EventHandle;
import se.l4.aurochs.core.events.EventHelper;

import com.google.common.collect.Maps;

public class MutableNodeStates<D>
	implements NodeStates<D>
{
	private final EventHelper<Consumer<NodeStateEvent<D>>> events;
	private final Map<Node<D>, NodeState> states;
	private final List<Node<D>> onlineNodes;
	
	public MutableNodeStates()
	{
		events = new EventHelper<>();
		states = Maps.newConcurrentMap();
		onlineNodes = new CopyOnWriteArrayList<>();
	}
	
	public void setState(Node<D> node, NodeState state)
	{
		if(states.put(node, state) != state)
		{
			NodeStateEvent<D> event = new NodeStateEvent<>(node, state);
			events.forEach(in -> in.accept(event));
			
			if(state == NodeState.OFFLINE)
			{
				onlineNodes.remove(node);
			}
			else if(! onlineNodes.contains(node))
			{
				onlineNodes.add(node);
			}
		}
	}
	
	public void removeNode(Node<D> node)
	{
		if(states.remove(node) != null)
		{
			NodeStateEvent<D> event = new NodeStateEvent<>(node, NodeState.OFFLINE);
			events.forEach(in -> in.accept(event));
			
			onlineNodes.remove(node);
		}
	}
	
	@Override
	public EventHandle listen(Consumer<NodeStateEvent<D>> consumer)
	{
		for(Map.Entry<Node<D>, NodeState> e : states.entrySet())
		{
			consumer.accept(new NodeStateEvent<>(e.getKey(), e.getValue()));
		}
		return events.listen(consumer);
	}
	
	@Override
	public NodeState get(Node<D> node)
	{
		return states.get(node);
	}
	
	@Override
	public Node<D> random()
	{
		ThreadLocalRandom random = ThreadLocalRandom.current();
		int r = random.nextInt(onlineNodes.size());
		return onlineNodes.get(r);
	}
}
