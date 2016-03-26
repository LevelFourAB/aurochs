package se.l4.aurochs.cluster.partitions;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import se.l4.aurochs.cluster.nodes.Node;
import se.l4.aurochs.cluster.nodes.NodeStates;
import se.l4.aurochs.core.log.StateLogBuilder;
import se.l4.commons.io.Bytes;

public interface PartitionCreateEncounter<T>
{
	/**
	 * Get the partition this encounter is for.
	 * 
	 * @return
	 */
	int partition();
	
	/**
	 * Get a data directory suitable for storing data for the service being created.
	 * 
	 * @return
	 */
	File getDataDir();
	
	/**
	 * Start creating a new state log for this service.
	 * 
	 * @return
	 */
	StateLogBuilder<Bytes> stateLog();
	
	/**
	 * Get the local node. This node instance is not intended to be used for sending messages,
	 * but it can be used together with {@link #nodes()} to check if we are the leader.
	 * 
	 * @return
	 */
	Node<?> localNode();
	
	/**
	 * Get a {@link NodeStates} instance tracking the members of this partition.
	 * 
	 * @return
	 */
	NodeStates<?> nodes();
	
	/**
	 * Create a channel for receiving and sending messages.
	 * 
	 * @return
	 */
	PartitionChannel<T> createChannel(Function<T, CompletableFuture<T>> messageHandler);
}
