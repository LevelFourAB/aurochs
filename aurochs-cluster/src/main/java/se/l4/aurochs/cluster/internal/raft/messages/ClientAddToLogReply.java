package se.l4.aurochs.cluster.internal.raft.messages;

public class ClientAddToLogReply
	implements RaftMessage
{
	private final String sender;
	private final long term;
	private final long id;
	private final long index;

	public ClientAddToLogReply(String sender, long term, long id, long index)
	{
		this.sender = sender;
		this.term = term;
		this.id = id;
		this.index = index;
	}
	
	public long getId()
	{
		return id;
	}
	
	public long getIndex()
	{
		return index;
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
