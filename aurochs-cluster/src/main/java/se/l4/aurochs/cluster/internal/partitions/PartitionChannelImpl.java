package se.l4.aurochs.cluster.internal.partitions;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import se.l4.aurochs.cluster.nodes.Node;
import se.l4.aurochs.cluster.partitions.PartitionChannel;
import se.l4.aurochs.cluster.partitions.PartitionMessage;
import se.l4.aurochs.cluster.partitions.Partitioner;
import se.l4.aurochs.core.channel.ChannelListener;
import se.l4.aurochs.core.channel.MessageEvent;
import se.l4.aurochs.core.channel.rpc.RPCHelper;
import se.l4.aurochs.core.channel.rpc.RpcMessage;
import se.l4.aurochs.core.events.EventHandle;


public class PartitionChannelImpl<T extends PartitionMessage>
	implements PartitionChannel<T>
{
	private final RPCHelper<T> rpc;
	private final Partitions<RpcMessage<T>> partitions;
	private final ChannelListener<RpcMessage<T>> listener;
	
	private final EventHandle events;
	private final PartitionerImpl partitioner;
	
	public PartitionChannelImpl(Partitions<RpcMessage<T>> partitions, Function<T, CompletableFuture<T>> handleRequest)
	{
		this.partitions = partitions;

		rpc = new RPCHelper<>(handleRequest);
		
		listener = this::listener;
		for(Node<RpcMessage<T>> node : partitions.listNodes())
		{
			node.getChannel().addListener(listener);
		}
		
		events = partitions.listen(event -> {
			if(event.getType() == PartitionEvent.Type.LEFT)
			{
				event.getNode().getChannel().removeListener(listener);
			}
			else if(event.getType() == PartitionEvent.Type.JOINED)
			{
				event.getNode().getChannel().addListener(listener);
			}
		});
	
		partitioner = new PartitionerImpl(partitions);
	}
	
	public PartitionChannelImpl<T> withRequestHandler(Function<T, CompletableFuture<T>> handler)
	{
		return new PartitionChannelImpl<>(partitions, handler);
	}
	
	public void close()
	{
		events.stop();
		
		for(Node<RpcMessage<T>> node : partitions.listNodes())
		{
			node.getChannel().removeListener(listener);
		}
	}
	
	@Override
	public Partitioner partitioner()
	{
		return partitioner;
	}
	
	private void listener(MessageEvent<RpcMessage<T>> event)
	{
		rpc.accept(event.getMessage(), msg -> event.getChannel().send(msg));
	}
	
	@Override
	public <R extends T> CompletableFuture<R> sendToOneOf(int partition, T message)
	{
		return rpc.request(message, msg -> partitions.forOneIn(partition, node -> node.getChannel().send(msg)));
	}

	@Override
	public <R extends T> CompletableFuture<R> sendToAll(int partition, T message)
	{
		return rpc.request(message, msg -> partitions.forAllIn(partition, node -> node.getChannel().send(msg)));
	}
}
