package se.l4.aurochs.cluster;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;

import se.l4.aurochs.core.channel.Channel;

/**
 * Interface for accessing the cluster and its distributed data structures.
 * 
 * @author Andreas Holstenson
 *
 */
public interface Cluster
{
	/**
	 * Get a specific queue from the cluster. The queue will be shared between
	 * all cluster nodes.
	 * 
	 * @param name
	 * @param type
	 * @return
	 */
	<T> BlockingQueue<T> getQueue(String name, Class<T> type);

	/**
	 * Get the a topic from the cluster. Topics can be used for simple
	 * publish/subscribe support. They are not persistent.
	 * 
	 * @param name
	 * @param type
	 * @return
	 */
	<T> Channel<T> getChannel(String name, Class<T> type);
	
	/**
	 * Get a distributed lock. Only a single node can hold the lock at any
	 * given time.
	 * 
	 * @param name
	 * @return
	 */
	Lock getLock(String name);
}
