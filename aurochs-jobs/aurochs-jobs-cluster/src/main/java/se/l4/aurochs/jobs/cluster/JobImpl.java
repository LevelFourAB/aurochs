package se.l4.aurochs.jobs.cluster;

import se.l4.aurochs.jobs.Job;
import se.l4.aurochs.jobs.cluster.op.JobDone;
import se.l4.aurochs.jobs.cluster.op.JobOperation;

public class JobImpl<T>
	implements Job<T>
{
	private final long id;
	private final T data;
	private final int attempt;
	
	private JobDone.Status status;
	private Object result;
	private long retryAt;

	public JobImpl(long id, T data, int attempt)
	{
		this.id = id;
		this.data = data;
		this.attempt = attempt;
	}

	@Override
	public T getData()
	{
		return data;
	}

	@Override
	public void complete()
	{
		if(status == null)
		{
			status = JobDone.Status.COMPLETED;
		}
	}

	@Override
	public void complete(Object result)
	{
		status = JobDone.Status.COMPLETED;
		this.result = result;
	}

	@Override
	public void failNoRetry(Throwable t)
	{
		if(status == null)
		{
			status = JobDone.Status.FAILED;
			retryAt = -1;
		}
	}

	@Override
	public void fail(Throwable t)
	{
		if(status == null)
		{
			status = JobDone.Status.FAILED;
		}
	}

	@Override
	public void fail(Throwable t, long waitTimeInMs)
	{
		status = JobDone.Status.FAILED;
		retryAt = System.currentTimeMillis() + waitTimeInMs;
	}

	@Override
	public boolean isLastTry()
	{
		return attempt >= 5;
	}

	JobOperation finish(JobStorage storage)
	{
		return new JobDone(id, status, result, retryAt);
	}
}
