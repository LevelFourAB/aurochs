package se.l4.aurochs.cluster.internal.raft.messages;

import se.l4.aurochs.core.io.Bytes;

public class ClientAddToLog
	implements RaftMessage
{
	private final String sender;
	private final long term;
	private final long id;
	private final Bytes data;

	public ClientAddToLog(String sender, long term, long id, Bytes data)
	{
		this.sender = sender;
		this.term = term;
		this.id = id;
		this.data = data;
	}
	
	public long getId()
	{
		return id;
	}
	
	public Bytes getData()
	{
		return data;
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
