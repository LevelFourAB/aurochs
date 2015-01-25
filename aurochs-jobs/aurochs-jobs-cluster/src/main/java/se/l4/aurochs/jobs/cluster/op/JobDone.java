package se.l4.aurochs.jobs.cluster.op;

import se.l4.aurochs.serialization.AllowAny;
import se.l4.aurochs.serialization.Expose;
import se.l4.aurochs.serialization.ReflectionSerializer;
import se.l4.aurochs.serialization.Use;
import se.l4.aurochs.serialization.enums.MapEnumVia;
import se.l4.aurochs.serialization.enums.OrdinalTranslator;

@Use(ReflectionSerializer.class)
public class JobDone
	implements JobOperation
{
	public enum Status
	{
		COMPLETED,
		FAILED
	}
	
	@Expose
	private final long id;
	
	@Expose
	@MapEnumVia(OrdinalTranslator.class)
	private final Status status;
	
	@Expose
	@AllowAny
	private final Object result;
	
	@Expose
	private final long retryAt;
	
	public JobDone(
			@Expose("id") long id,
			@Expose("status") Status status,
			@Expose("result") Object result,
			@Expose("retryAt") long retryAt)
	{
		this.id = id;
		this.status = status;
		this.result = result;
		this.retryAt = retryAt;
	}
	
	public long getId()
	{
		return id;
	}
	
	public Status getStatus()
	{
		return status;
	}
	
	public Object getResult()
	{
		return result;
	}
	
	public long getRetryAt()
	{
		return retryAt;
	}
}
