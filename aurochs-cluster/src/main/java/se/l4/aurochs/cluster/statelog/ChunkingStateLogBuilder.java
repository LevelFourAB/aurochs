package se.l4.aurochs.cluster.statelog;

import java.io.File;
import java.io.InputStream;

import se.l4.aurochs.cluster.nodes.NodeSet;
import se.l4.aurochs.core.io.Bytes;
import se.l4.aurochs.core.io.IoConsumer;

/**
 * Builder of a {@link ChunkingStateLog}.
 * 
 * @author Andreas Holstenson
 *
 */
public interface ChunkingStateLogBuilder
{
	/**
	 * Set the nodes that this state log should be built over and specify the
	 * id of ourselves.
	 * 
	 * @param nodes
	 * @param selfId
	 * @return
	 */
	ChunkingStateLogBuilder withNodes(NodeSet<Bytes> nodes, String selfId);
	
	/**
	 * Set the consumer that is used to commit entries in the log. The applier
	 * will be called at most once for every successful commit. If the
	 * log is closed and then opened again the applier will not be called for
	 * previous log entries.
	 * 
	 * <p>
	 * If your service has a in-memory representation of data use
	 * {@link #withVolatileApplier(IoConsumer)} instead.
	 * 
	 * @param applier
	 * @return
	 */
	ChunkingStateLogBuilder withApplier(IoConsumer<InputStream> applier);
	
	/**
	 * Store state in a file.
	 * 
	 * @param file
	 * @return
	 */
	ChunkingStateLogBuilder stateInFile(File file);
	
	/**
	 * Build the state log.
	 * 
	 * @return
	 */
	ChunkingStateLog build();
}
