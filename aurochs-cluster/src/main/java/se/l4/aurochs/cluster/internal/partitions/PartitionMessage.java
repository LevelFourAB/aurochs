package se.l4.aurochs.cluster.internal.partitions;

public class PartitionMessage<T>
{
	private final int partition;
	private final T payload;

	public PartitionMessage(int partition, T payload)
	{
		this.partition = partition;
		this.payload = payload;
	}
	
	public int getPartition()
	{
		return partition;
	}
	
	public T getPayload()
	{
		return payload;
	}
}
