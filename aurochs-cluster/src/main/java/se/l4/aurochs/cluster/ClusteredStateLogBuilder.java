package se.l4.aurochs.cluster;

import java.io.File;

import se.l4.aurochs.cluster.nodes.NodeSet;
import se.l4.aurochs.core.io.Bytes;
import se.l4.aurochs.core.log.StateLogBuilder;

public interface ClusteredStateLogBuilder<T>
	extends StateLogBuilder<T>
{
	/**
	 * Set the nodes that this state log should be built over and specify the
	 * id of ourselves.
	 * 
	 * @param nodes
	 * @param selfId
	 * @return
	 */
	ClusteredStateLogBuilder<T> withNodes(NodeSet<Bytes> nodes, String selfId);
	
	/**
	 * Request that the log is stored in memory.
	 * 
	 * @return
	 */
	ClusteredStateLogBuilder<T> inMemory();
	
	/**
	 * Store state in a file.
	 * 
	 * @param file
	 * @return
	 */
	ClusteredStateLogBuilder<T> stateInFile(File file);
}
