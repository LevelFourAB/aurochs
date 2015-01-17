package se.l4.aurochs.cluster.partitions;

public interface Partitioner
{
	int partition(long id);
	
	int partition(String id);
	
	int partition(byte[] data);
}
