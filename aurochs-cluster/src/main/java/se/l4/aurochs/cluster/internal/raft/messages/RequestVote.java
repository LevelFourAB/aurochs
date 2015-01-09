package se.l4.aurochs.cluster.internal.raft.messages;

public class RequestVote
	implements RaftMessage
{
	private final long term;
	private final String candidateId;
	private final long lastLogIndex;
	private final long lastLogTerm;
	
	public RequestVote(long term, String candidateId, long lastLogIndex, long lastLogTerm)
	{
		this.term = term;
		this.candidateId = candidateId;
		this.lastLogIndex = lastLogIndex;
		this.lastLogTerm = lastLogTerm;
	}
	
	@Override
	public String getSenderId()
	{
		return candidateId;
	}
	
	public long getTerm()
	{
		return term;
	}
	
	public String getCandidateId()
	{
		return candidateId;
	}
	
	public long getLastLogIndex()
	{
		return lastLogIndex;
	}
	
	public long getLastLogTerm()
	{
		return lastLogTerm;
	}
}
