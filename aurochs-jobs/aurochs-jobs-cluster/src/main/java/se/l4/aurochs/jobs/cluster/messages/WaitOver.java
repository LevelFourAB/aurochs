package se.l4.aurochs.jobs.cluster.messages;

import se.l4.aurochs.serialization.AllowAny;
import se.l4.aurochs.serialization.Expose;
import se.l4.aurochs.serialization.ReflectionSerializer;
import se.l4.aurochs.serialization.Use;

@Use(ReflectionSerializer.class)
public class WaitOver
	implements JobControlMessage
{
	@AllowAny
	@Expose
	private final Object data;
	
	public WaitOver(@Expose("data") Object data)
	{
		this.data = data;
	}
	
	public Object getData()
	{
		return data;
	}
}
