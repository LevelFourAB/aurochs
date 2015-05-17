package se.l4.aurochs.cluster.internal.raft.messages;

import java.util.List;

import se.l4.aurochs.cluster.internal.raft.log.StoredLogEntry;

public class AppendEntries
	implements RaftMessage
{
	private final String sender;
	private final long term;
	private final long prevLogIndex;
	private final long prevLogTerm;
	private final List<StoredLogEntry> entries;
	private final long leaderCommit;

	public AppendEntries(String sender, long term, long prevLogIndex, long prevLogTerm, List<StoredLogEntry> entries, long leaderCommit)
	{
		this.sender = sender;
		this.term = term;
		this.prevLogIndex = prevLogIndex;
		this.prevLogTerm = prevLogTerm;
		this.entries = entries;
		this.leaderCommit = leaderCommit;
	}
	
	@Override
	public long getTerm()
	{
		return term;
	}
	
	@Override
	public String getSenderId()
	{
		return sender;
	}
	
	public long getPrevLogIndex()
	{
		return prevLogIndex;
	}
	
	public long getPrevLogTerm()
	{
		return prevLogTerm;
	}
	
	public List<StoredLogEntry> getEntries()
	{
		return entries;
	}
	
	public long getLeaderCommit()
	{
		return leaderCommit;
	}
}
