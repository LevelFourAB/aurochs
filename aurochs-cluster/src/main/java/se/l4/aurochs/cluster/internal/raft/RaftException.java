package se.l4.aurochs.cluster.internal.raft;

public class RaftException
	extends RuntimeException
{
	public RaftException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public RaftException(String message)
	{
		super(message);
	}
}
