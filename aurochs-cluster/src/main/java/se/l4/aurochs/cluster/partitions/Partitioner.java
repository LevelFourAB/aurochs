package se.l4.aurochs.cluster.partitions;

public interface Partitioner
{
	/**
	 * Get the total number of partitions.
	 * 
	 * @return
	 */
	int total();
	
	/**
	 * Get the partition where data of the given id is stored.
	 * 
	 * @param id
	 * @return
	 */
	int partition(long id);
	
	/**
	 * Get the partition where data of the given is stored.
	 * 
	 * @param id
	 * @return
	 */
	int partition(String id);
	
	/**
	 * Get the partition where data of the given id is stored.
	 * 
	 * @param data
	 * @return
	 */
	int partition(byte[] data);
	
	/**
	 * Get the partition where data is stored, supports ints, longs, {@link String}
	 * and byte-arrays.
	 * 
	 * @param id
	 * @return
	 */
	int partition(Object id);
}
