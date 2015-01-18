package se.l4.aurochs.cluster.internal.raft.messages;

public class ClientAddToLogReply
	implements RaftMessage
{
	private final String sender;
	private final long term;
	private final long id;

	public ClientAddToLogReply(String sender, long term, long id)
	{
		this.sender = sender;
		this.term = term;
		this.id = id;
	}
	
	public long getId()
	{
		return id;
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
}
