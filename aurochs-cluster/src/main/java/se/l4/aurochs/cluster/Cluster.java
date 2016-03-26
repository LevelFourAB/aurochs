package se.l4.aurochs.cluster;

import se.l4.commons.io.Bytes;


/**
 * Interface for accessing the cluster and its distributed data structures.
 * 
 * @author Andreas Holstenson
 *
 */
public interface Cluster
{
	enum MemberType
	{
		NONE,
		
		CLIENT,
		
		SERVER
	}
	
	/**
	 * Get what type of node this process interface represents.
	 * 
	 * @return
	 */
	MemberType getLocalType();
	
	ServiceBuilder<Bytes> newService(String name);
}
