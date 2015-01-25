package se.l4.aurochs.cluster.internal.partitions;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.IntFunction;

import se.l4.aurochs.cluster.nodes.Node;
import se.l4.aurochs.cluster.nodes.NodeSet;
import se.l4.aurochs.cluster.nodes.NodeStates;
import se.l4.aurochs.cluster.partitions.PartitionChannel;
import se.l4.aurochs.cluster.partitions.Partitioner;
import se.l4.aurochs.core.channel.ChannelListener;
import se.l4.aurochs.core.channel.MessageEvent;
import se.l4.aurochs.core.channel.rpc.RPC;
import se.l4.aurochs.core.channel.rpc.RPCHelper;
import se.l4.aurochs.core.channel.rpc.RpcMessage;
import se.l4.aurochs.core.events.EventHandle;


public class ServicePartitionChannel<T>
	implements PartitionChannel<T>
{
	private final RPCHelper<PartitionMessage<T>> rpc;
	private final Partitions<RpcMessage<PartitionMessage<T>>> partitions;
	private final Function<T, CompletableFuture<T>>[] requestHandlers;
	
	private final EventHandle events;
	private final PartitionerImpl partitioner;
	
	private final Map<Node<?>, ChannelListener<?>> nodeToListener;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ServicePartitionChannel(Partitions<RpcMessage<PartitionMessage<T>>> partitions)
	{
		this.partitions = partitions;

		rpc = RPC.createHelper(this::handleRequest);
		
		nodeToListener = new ConcurrentHashMap<>();
		
		for(Node<RpcMessage<PartitionMessage<T>>> node : partitions.listNodes())
		{
			ChannelListener<RpcMessage<PartitionMessage<T>>> listener = (e) -> listener(node, e);
			node.incoming().addListener(listener);
			nodeToListener.put(node, listener);
		}
		
		events = partitions.listen(event -> {
			if(event.getType() == PartitionEvent.Type.LEFT)
			{
				event.getNode().incoming().removeListener((ChannelListener) nodeToListener.remove(event.getNode()));
			}
			else if(event.getType() == PartitionEvent.Type.JOINED)
			{
				Node<RpcMessage<PartitionMessage<T>>> node = event.getNode();
				if(nodeToListener.containsKey(node)) return;
				
				ChannelListener<RpcMessage<PartitionMessage<T>>> listener = (e) -> listener(node, e);
				event.getNode().incoming().addListener(listener);
				nodeToListener.put(node, listener);
			}
		});
	
		partitioner = new PartitionerImpl(partitions);
		
		requestHandlers = new Function[partitions.getTotal()];
	}
	
	public LocalPartitionChannel<T> forPartition(int partition, Function<T, CompletableFuture<T>> handler)
	{
		requestHandlers[partition] = handler;
		return new LocalPartitionChannel<>(this, partition);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void close()
	{
		events.stop();
		
		for(Node<RpcMessage<PartitionMessage<T>>> node : partitions.listNodes())
		{
			node.incoming().removeListener((ChannelListener) nodeToListener.remove(node));
		}
	}
	
	public Node<RpcMessage<PartitionMessage<T>>> localNode()
	{
		return partitions.getLocal();
	}
	
	public NodeSet<RpcMessage<PartitionMessage<T>>> nodes(int partition)
	{
		return partitions.getNodes(partition);
	}
	
	public NodeStates<RpcMessage<PartitionMessage<T>>> nodeStates(int partition)
	{
		return partitions.getNodeStates(partition);
	}
	
	@Override
	public Partitioner partitioner()
	{
		return partitioner;
	}
	
	private CompletableFuture<PartitionMessage<T>> handleRequest(PartitionMessage<T> message)
	{
		Function<T, CompletableFuture<T>> function = requestHandlers[message.getPartition()];
		if(function == null) return CompletableFuture.completedFuture(null);
		
		int p = message.getPartition();
		return function.apply(message.getPayload())
			.thenApply(msg -> new PartitionMessage<>(p, msg));
	}
	
	private void listener(Node<RpcMessage<PartitionMessage<T>>> node, MessageEvent<RpcMessage<PartitionMessage<T>>> event)
	{
		rpc.accept(event.getMessage(), msg -> node.outgoing().send(msg));
	}
	
	@Override
	public <R> R withRandomPartition(IntFunction<R> op)
	{
		int p = ThreadLocalRandom.current().nextInt(partitions.getTotal());
		return op.apply(p);
	}
	
	@Override
	public <R extends T> CompletableFuture<R> sendToRandomPartition(T message)
	{
		int p = ThreadLocalRandom.current().nextInt(partitions.getTotal());
		return sendToOneOf(p, message);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <R extends T> CompletableFuture<R> sendToOneOf(int partition, T message)
	{
		return rpc.request(
			new PartitionMessage<>(partition, message),
			msg -> partitions.forOneIn(partition, node -> node.outgoing().send(msg))
		).thenApply(p -> (R) p.getPayload());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R extends T> CompletableFuture<R> sendToAll(int partition, T message)
	{
		return rpc.request(
			new PartitionMessage<>(partition, message),
			msg -> partitions.forAllIn(partition, node -> node.outgoing().send(msg))
		).thenApply(p -> (R) p.getPayload());
	}
}
