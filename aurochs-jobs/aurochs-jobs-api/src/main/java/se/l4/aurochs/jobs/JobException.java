package se.l4.aurochs.jobs;

public class JobException
	extends RuntimeException
{
	public JobException(String msg)
	{
		super(msg);
	}
	
	public JobException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}
