package se.l4.aurochs.cluster.internal.raft.messages;

public class RequestVoteReply
	implements RaftMessage
{
	private final String sender;
	private final long term;
	private final boolean voteGranted;
	
	public RequestVoteReply(String sender, long term, boolean voteGranted)
	{
		this.sender = sender;
		this.term = term;
		this.voteGranted = voteGranted;
	}
	
	@Override
	public String getSenderId()
	{
		return sender;
	}
	
	public long getTerm()
	{
		return term;
	}
	
	public boolean isVoteGranted()
	{
		return voteGranted;
	}
}
