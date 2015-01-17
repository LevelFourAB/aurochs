package se.l4.aurochs.cluster.partitions;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import se.l4.aurochs.cluster.StateLogBuilder;
import se.l4.aurochs.core.io.Bytes;

public interface PartitionCreateEncounter<T>
{
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
	 * Create a channel for receiving and sending messages.
	 * 
	 * @return
	 */
	PartitionChannel<T> createChannel(Function<T, CompletableFuture<T>> messageHandler);
}
