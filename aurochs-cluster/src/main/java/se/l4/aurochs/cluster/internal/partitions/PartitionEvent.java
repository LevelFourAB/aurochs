package se.l4.aurochs.cluster.internal.partitions;

import se.l4.aurochs.cluster.nodes.Node;

public class PartitionEvent<T>
{
	public enum Type
	{
		JOINED,
		LEFT
	}
	
	private final Type type;
	private final int partition;
	private final Node<T> node;
	
	public PartitionEvent(Type type, int partition, Node<T> node)
	{
		this.type = type;
		this.partition = partition;
		this.node = node;
	}
	
	public Type getType()
	{
		return type;
	}
	
	public int getPartition()
	{
		return partition;
	}
	
	public Node<T> getNode()
	{
		return node;
	}
}
