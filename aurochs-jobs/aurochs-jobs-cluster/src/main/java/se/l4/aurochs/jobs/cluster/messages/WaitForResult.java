package se.l4.aurochs.jobs.cluster.messages;

import se.l4.aurochs.serialization.Expose;
import se.l4.aurochs.serialization.ReflectionSerializer;
import se.l4.aurochs.serialization.Use;

@Use(ReflectionSerializer.class)
public class WaitForResult
	implements JobControlMessage
{
	@Expose
	private final long id;
	
	public WaitForResult(@Expose("id") long id)
	{
		this.id = id;
	}
	
	public long getId()
	{
		return id;
	}
}
