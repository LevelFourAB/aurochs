package se.l4.aurochs.cluster.internal.raft.messages;

import java.util.List;

import se.l4.aurochs.cluster.internal.raft.log.LogEntry;

public class AppendEntries
	implements RaftMessage
{
	private final String sender;
	private final long term;
	private final long prevLogIndex;
	private final long prevLogTerm;
	private final List<LogEntry> entries;
	private final long leaderCommit;

	public AppendEntries(String sender, long term, long prevLogIndex, long prevLogTerm, List<LogEntry> entries, long leaderCommit)
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
	
	public List<LogEntry> getEntries()
	{
		return entries;
	}
	
	public long getLeaderCommit()
	{
		return leaderCommit;
	}
}
