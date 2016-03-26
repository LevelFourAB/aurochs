package se.l4.aurochs.cluster.internal.service;

import se.l4.aurochs.cluster.nodes.NodeSet;
import se.l4.commons.io.Bytes;

public interface ServiceHandle
{
	/**
	 * Get the nodes that this service runs on.
	 * 
	 * @return
	 */
	NodeSet<Bytes> getNodes();
	
	/**
	 * Stop this service, used for node shutdown.
	 */
	void stop();
	
	/**
	 * Remove this service and all of its data. This is used when the service has been migrated to
	 * another node.
	 */
	void remove();
}
