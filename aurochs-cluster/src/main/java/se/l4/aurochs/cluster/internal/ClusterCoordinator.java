package se.l4.aurochs.cluster.internal;

import java.io.File;

import se.l4.aurochs.cluster.StateLog;
import se.l4.aurochs.cluster.internal.raft.RaftBuilder;
import se.l4.aurochs.cluster.nodes.Nodes;
import se.l4.aurochs.core.internal.NamedChannelCodec;
import se.l4.aurochs.core.io.ByteMessage;
import se.l4.aurochs.core.io.Bytes;

/**
 * Coordinator of core activities in the cluster.
 * 
 * @author Andreas Holstenson
 *
 */
public class ClusterCoordinator
{
	private final StateLog<Bytes> stateLog;

	public ClusterCoordinator(Nodes<ByteMessage> nodes, String id, File storage)
	{
		stateLog = new RaftBuilder<Bytes>()
			.withNodes(nodes.transform(new NamedChannelCodec("cluster")), id)
			.stateInFile(new File(storage, "core.log"))
			.withVolatileApplier(this::applyState)
			.build();
	}
	
	private void applyState(Bytes bytes)
	{
		
	}
}
