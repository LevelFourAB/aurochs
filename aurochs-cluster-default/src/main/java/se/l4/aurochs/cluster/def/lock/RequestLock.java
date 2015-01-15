package se.l4.aurochs.cluster.def.lock;

import se.l4.aurochs.serialization.Expose;
import se.l4.aurochs.serialization.Named;

@Named(namespace="aurochs:lock", name="request-lock")
public class RequestLock
	implements LockMessage
{
	@Expose
	private final int partition;
	@Expose
	private final String lock;
	
	public RequestLock(@Expose("partition") int partition, @Expose("lock") String lock)
	{
		this.partition = partition;
		this.lock = lock;
	}

	@Override
	public int getPartition()
	{
		return partition;
	}
	
	public String getLock()
	{
		return lock;
	}
}
