package se.l4.aurochs.cluster.partitions;

import se.l4.aurochs.cluster.StateLogBuilder;
import se.l4.aurochs.core.io.Bytes;

public interface PartitionCreateEncounter<T extends PartitionMessage>
{
	StateLogBuilder<Bytes> stateLog();
	
	PartitionChannel<T> createChannel();
}
