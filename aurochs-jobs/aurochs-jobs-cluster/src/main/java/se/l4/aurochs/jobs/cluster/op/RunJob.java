package se.l4.aurochs.jobs.cluster.op;

import se.l4.aurochs.serialization.Expose;
import se.l4.aurochs.serialization.ReflectionSerializer;
import se.l4.aurochs.serialization.Use;

/**
 * Operation to indicate that a node should run the job.
 * 
 * @author Andreas Holstenson
 *
 */
@Use(ReflectionSerializer.class)
public class RunJob
	implements JobOperation
{
	@Expose
	private final long id;
	
	@Expose
	private final String node;
	
	@Expose
	private final long timeout;
	
	public RunJob(@Expose("id") long id, @Expose("node") String node, @Expose("timeout") long timeout)
	{
		this.id = id;
		this.node = node;
		this.timeout = timeout;
	}
	
	public long getId()
	{
		return id;
	}
	
	public String getNode()
	{
		return node;
	}
	
	public long getTimeout()
	{
		return timeout;
	}
}
