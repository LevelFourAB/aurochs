package se.l4.aurochs.cluster.internal.raft.log;

import se.l4.aurochs.core.io.Bytes;

public class DefaultLogEntry
	implements LogEntry
{
	private final long id;
	private final long term;
	private final Bytes data;

	public DefaultLogEntry(long id, long term, Bytes data)
	{
		this.id = id;
		this.term = term;
		this.data = data;
	}

	@Override
	public long getId()
	{
		return id;
	}

	@Override
	public long getTerm()
	{
		return term;
	}

	@Override
	public Bytes getData()
	{
		return data;
	}

}
