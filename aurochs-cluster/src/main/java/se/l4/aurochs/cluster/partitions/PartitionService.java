package se.l4.aurochs.cluster.partitions;

import se.l4.aurochs.core.channel.MessageEvent;

public interface PartitionService<T>
{
	void receiveMessage(MessageEvent<T> message);
}
