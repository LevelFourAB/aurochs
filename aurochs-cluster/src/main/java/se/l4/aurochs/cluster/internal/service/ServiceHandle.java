package se.l4.aurochs.cluster.internal.service;

import se.l4.aurochs.cluster.nodes.Nodes;
import se.l4.aurochs.core.io.Bytes;

public interface ServiceHandle
{
	/**
	 * Get the nodes that this service runs on.
	 * 
	 * @return
	 */
	Nodes<Bytes> getNodes();
	
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
