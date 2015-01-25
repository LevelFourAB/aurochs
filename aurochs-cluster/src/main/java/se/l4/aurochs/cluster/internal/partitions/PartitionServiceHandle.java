package se.l4.aurochs.cluster.internal.partitions;

import se.l4.aurochs.cluster.StateLog;
import se.l4.aurochs.cluster.internal.service.ServiceHandle;
import se.l4.aurochs.cluster.nodes.NodeSet;
import se.l4.aurochs.cluster.partitions.PartitionService;
import se.l4.aurochs.core.io.Bytes;

public class PartitionServiceHandle
	implements ServiceHandle
{
	private final PartitionService<?> service;
	private final NodeSet<Bytes> nodes;
	private final StateLog<?> stateLog;

	public PartitionServiceHandle(PartitionService<?> service,
			NodeSet<Bytes> nodes,
			StateLog<?> stateLog)
	{
		this.service = service;
		this.nodes = nodes;
		this.stateLog = stateLog;
	}
	
	@Override
	public NodeSet<Bytes> getNodes()
	{
		return nodes;
	}
	
	@Override
	public void remove()
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void stop()
	{
		if(stateLog != null)
		{
			stateLog.close();
		}
	}
}
