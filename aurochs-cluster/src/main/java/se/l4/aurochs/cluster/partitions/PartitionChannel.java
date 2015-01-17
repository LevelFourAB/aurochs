package se.l4.aurochs.cluster.partitions;

import java.util.concurrent.CompletableFuture;

public interface PartitionChannel<T>
{
	Partitioner partitioner();
	
	<R extends T> CompletableFuture<R> sendToOneOf(int partition, T message);
	
	<R extends T> CompletableFuture<R> sendToAll(int partition, T message);
}
