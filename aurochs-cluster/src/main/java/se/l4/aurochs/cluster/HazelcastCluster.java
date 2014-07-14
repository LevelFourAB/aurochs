package se.l4.aurochs.cluster;

import com.hazelcast.core.HazelcastInstance;

/**
 * Extension to {@link Cluster} for interacting directly with Hazelcast.
 * 
 * @author Andreas Holstenson
 *
 */
public interface HazelcastCluster
	extends Cluster
{
	/**
	 * Get the Hazelcast instance.
	 * 
	 * @return
	 */
	HazelcastInstance getInstance();
}
