package se.l4.aurochs.cluster.partitions;

import java.util.function.IntFunction;

import se.l4.aurochs.cluster.ClusterChannel;

public interface PartitionChannel<T extends PartitionMessage>
	extends ClusterChannel<T>
{
	void sendToAll(IntFunction<PartitionMessage> messageCreator);
}
