package se.l4.aurochs.cluster.internal.raft.messages;

public class AppendEntriesReply
	implements RaftMessage
{
	private final String sender;
	private final long term;
	private final boolean success;
	
	public AppendEntriesReply(String sender, long term, boolean success)
	{
		this.sender = sender;
		this.term = term;
		this.success = success;
	}
	
	@Override
	public String getSenderId()
	{
		return sender;
	}
	
	@Override
	public long getTerm()
	{
		return term;
	}
	
	public boolean isSuccess()
	{
		return success;
	}
}
