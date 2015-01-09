package se.l4.aurochs.cluster.internal.raft.messages;

public class AppendEntriesReply
	implements RaftMessage
{
	private final String sender;
	private final long term;
	
	private final long prevLogIndex;
	private final int entries;
	
	private final boolean success;
	
	public AppendEntriesReply(String sender, long term, long prevLogIndex, int entries, boolean success)
	{
		this.sender = sender;
		this.term = term;
		this.prevLogIndex = prevLogIndex;
		this.entries = entries;
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
	
	public long getPrevLogIndex()
	{
		return prevLogIndex;
	}
	
	public int getEntries()
	{
		return entries;
	}
	
	public boolean isSuccess()
	{
		return success;
	}
}
