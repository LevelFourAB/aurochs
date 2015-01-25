package se.l4.aurochs.jobs.cluster.messages;

import se.l4.aurochs.jobs.cluster.op.JobOperation;
import se.l4.aurochs.serialization.AllowAny;
import se.l4.aurochs.serialization.Expose;
import se.l4.aurochs.serialization.ReflectionSerializer;
import se.l4.aurochs.serialization.Use;

@Use(ReflectionSerializer.class)
public class QueueJob
	implements JobControlMessage, JobOperation
{
	@Expose
	private final long id;
	
	@Expose
	@AllowAny
	private final Object data;
	
	@Expose
	private final long time;
	
	public QueueJob(@Expose("id") long id, @Expose("data") Object data, @Expose("time") long time)
	{
		this.id = id;
		this.data = data;
		this.time = time;
	}
	
	public long getId()
	{
		return id;
	}
	
	public Object getData()
	{
		return data;
	}
	
	public long getTime()
	{
		return time;
	}
}
