package se.l4.aurochs.cluster.internal.raft.log;

import se.l4.commons.io.Bytes;

public class DefaultStoredLogEntry
	implements StoredLogEntry
{
	private final long id;
	private final long term;
	private final Type type;
	private final Bytes data;

	public DefaultStoredLogEntry(long id, long term, Type type, Bytes data)
	{
		this.id = id;
		this.term = term;
		this.type = type;
		this.data = data;
	}

	@Override
	public long getIndex()
	{
		return id;
	}

	@Override
	public long getTerm()
	{
		return term;
	}
	
	@Override
	public Type getType()
	{
		return type;
	}

	@Override
	public Bytes getData()
	{
		return data;
	}

}
