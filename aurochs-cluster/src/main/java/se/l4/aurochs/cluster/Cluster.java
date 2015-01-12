package se.l4.aurochs.cluster;


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
}
