package se.l4.aurochs.cluster.internal.partitions;

import java.util.concurrent.CompletableFuture;
import java.util.function.IntFunction;

import se.l4.aurochs.cluster.nodes.NodeSet;
import se.l4.aurochs.cluster.nodes.NodeStates;
import se.l4.aurochs.cluster.partitions.PartitionChannel;
import se.l4.aurochs.cluster.partitions.Partitioner;
import se.l4.aurochs.core.channel.rpc.RpcMessage;

public class LocalPartitionChannel<T>
	implements PartitionChannel<T>
{
	private final ServicePartitionChannel<T> parent;
	private final int partition;
	
	private final NodeSet<RpcMessage<PartitionMessage<T>>> nodes;
	private final NodeStates<RpcMessage<PartitionMessage<T>>> nodeStates;

	public LocalPartitionChannel(ServicePartitionChannel<T> parent, int partition)
	{
		this.parent = parent;
		this.partition = partition;
		
		this.nodes = parent.nodes(partition);
		this.nodeStates = parent.nodeStates(partition);
	}

	public NodeSet<?> nodes()
	{
		return nodes;
	}
	
	public NodeStates<?> nodeStates()
	{
		return nodeStates;
	}
	
	@Override
	public Partitioner partitioner()
	{
		return parent.partitioner();
	}
	
	@Override
	public <R> R withRandomPartition(IntFunction<R> op)
	{
		return parent.withRandomPartition(op);
	}
	
	@Override
	public <R extends T> CompletableFuture<R> sendToRandomPartition(T message)
	{
		return parent.sendToRandomPartition(message);
	}
	
	@Override
	public <R extends T> CompletableFuture<R> sendToAll(int partition, T message)
	{
		return parent.sendToAll(partition, message);
	}
	
	@Override
	public <R extends T> CompletableFuture<R> sendToOneOf(int partition, T message)
	{
		return parent.sendToOneOf(partition, message);
	}
}
