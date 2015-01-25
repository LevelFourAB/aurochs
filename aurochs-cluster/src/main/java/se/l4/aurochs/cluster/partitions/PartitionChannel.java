package se.l4.aurochs.cluster.partitions;

import java.util.concurrent.CompletableFuture;
import java.util.function.IntFunction;

public interface PartitionChannel<T>
{
	/**
	 * Get the partitioner used to determine partitions from objects.
	 * 
	 * @return
	 */
	Partitioner partitioner();
	
	/**
	 * Select a random partition and call the given function.
	 * 
	 * @param op
	 */
	<R> R withRandomPartition(IntFunction<R> op);
	
	/**
	 * Send the message to a random partition.
	 * 
	 * @param message
	 * @return
	 */
	<R extends T> CompletableFuture<R> sendToRandomPartition(T message);
	
	/**
	 * Send to one of the nodes in the given partition.
	 * 
	 * @param partition
	 * @param message
	 * @return
	 */
	<R extends T> CompletableFuture<R> sendToOneOf(int partition, T message);
	
	/**
	 * Send to all of the nodes in the given partition.
	 * 
	 * @param partition
	 * @param message
	 * @return
	 */
	<R extends T> CompletableFuture<R> sendToAll(int partition, T message);
}
