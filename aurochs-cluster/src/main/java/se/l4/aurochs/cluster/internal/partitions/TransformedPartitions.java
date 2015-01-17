package se.l4.aurochs.cluster.internal.partitions;

import java.util.Map;
import java.util.function.Consumer;

import se.l4.aurochs.cluster.nodes.Node;
import se.l4.aurochs.cluster.nodes.Nodes;
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

	public TransformedPartitions(Partitions<O> partitions, ChannelCodec<O, T> codec)
	{
		this.partitions = partitions;
		this.codec = codec;
		
		nodes = Maps.newHashMap();
	}
	
	private Node<T> get(Node<O> node)
	{
		synchronized(nodes)
		{
			Node<T> transformed = nodes.get(node);
			if(transformed == null)
			{
				transformed = new Node<>(node.getId(), node.getChannel().transform(codec));
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
	public Nodes<T> getNodes(int partition)
	{
		throw new UnsupportedOperationException();
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
	
}
