package se.l4.aurochs.cluster.internal.partitions;

import java.util.Map;
import java.util.function.Consumer;

import se.l4.aurochs.cluster.nodes.Node;
import se.l4.aurochs.cluster.nodes.NodeEvent;
import se.l4.aurochs.cluster.nodes.NodeSet;
import se.l4.aurochs.cluster.nodes.NodeState;
import se.l4.aurochs.cluster.nodes.NodeStateEvent;
import se.l4.aurochs.cluster.nodes.NodeStates;
import se.l4.aurochs.core.channel.ChannelCodec;
import se.l4.aurochs.core.events.EventHandle;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public class TransformedPartitions<O, T>
	implements Partitions<T>
{
	private final Partitions<O> partitions;
	private final ChannelCodec<O, T> codec;
	private final Map<Node<O>, Node<T>> nodes;
	private final NodeSet<T>[] partitionToNodeSet;
	private final NodeStates<T>[] partitionToNodeStates;

	@SuppressWarnings("unchecked")
	public TransformedPartitions(Partitions<O> partitions, ChannelCodec<O, T> codec)
	{
		this.partitions = partitions;
		this.codec = codec;
		
		nodes = Maps.newHashMap();
		
		NodeSet<T>[] partitionToNode = new NodeSet[partitions.getTotal()];
		for(int i=0, n=partitionToNode.length; i<n; i++) partitionToNode[i] = new TransformedNodes(partitions.getNodes(i));
		this.partitionToNodeSet =  partitionToNode;
		
		NodeStates<T>[] partitionToNodeStates = new NodeStates[partitions.getTotal()];
		for(int i=0, n=partitionToNodeStates.length; i<n; i++) partitionToNodeStates[i] = new TransformedNodeStates(partitions.getNodeStates(i));
		this.partitionToNodeStates = partitionToNodeStates;
	}
	
	@Override
	public Node<T> getLocal()
	{
		return get(partitions.getLocal());
	}
	
	private Node<T> get(Node<O> node)
	{
		if(node == null) return null;
		
		synchronized(nodes)
		{
			Node<T> transformed = nodes.get(node);
			if(transformed == null)
			{
				transformed = node.transform(codec);
				nodes.put(node, transformed);
			}
			
			return transformed;
		}
	}
	
	@Override
	public int getTotal()
	{
		return partitions.getTotal();
	}
	
	@Override
	public EventHandle listen(Consumer<PartitionEvent<T>> listener)
	{
		return partitions.listen(event -> listener.accept(new PartitionEvent<>(
			event.getType(),
			event.getPartition(),
			get(event.getNode())
		)));
	}
	
	@Override
	public NodeSet<T> getNodes(int partition)
	{
		return partitionToNodeSet[partition];
	}
	
	@Override
	public NodeStates<T> getNodeStates(int partition)
	{
		return partitionToNodeStates[partition];
	}
	
	@Override
	public Iterable<Node<T>> listNodes()
	{
		return Iterables.transform(partitions.listNodes(), this::get);
	}
	
	@Override
	public void forAllIn(int partition, Consumer<Node<T>> action)
	{
		partitions.forAllIn(partition, node -> action.accept(get(node)));
	}
	
	@Override
	public void forOneIn(int partition, Consumer<Node<T>> action)
	{
		partitions.forOneIn(partition, node -> action.accept(get(node)));
	}
	
	private class TransformedNodes
		implements NodeSet<T>
	{
		private final NodeSet<O> nodes;
	
		public TransformedNodes(NodeSet<O> nodes)
		{
			this.nodes = nodes;
		}
		
		@Override
		public Node<T> get(String id)
		{
			return TransformedPartitions.this.get(nodes.get(id));
		}
		
		@Override
		public EventHandle listen(Consumer<NodeEvent<T>> consumer)
		{
			return nodes.listen(e -> consumer.accept(new NodeEvent<>(
				e.getType(),
				TransformedPartitions.this.get(e.getNode())
			)));
		}
	}

	private class TransformedNodeStates
		implements NodeStates<T>
	{
		private final NodeStates<O> states;
	
		public TransformedNodeStates(NodeStates<O> states)
		{
			this.states = states;;
		}
		
		@Override
		public EventHandle listen(Consumer<NodeStateEvent<T>> consumer)
		{
			return states.listen(e -> consumer.accept(new NodeStateEvent<>(
				TransformedPartitions.this.get(e.getNode()),
				e.getState()
			)));
		}
		
		@Override
		public Node<T> random()
		{
			return TransformedPartitions.this.get(states.random());
		}

		@SuppressWarnings("unchecked")
		@Override
		public NodeState get(Node<T> node)
		{
			return states.get((Node) node);
		}
	}
}
