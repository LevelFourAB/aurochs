package se.l4.aurochs.cluster.internal.raft.messages;

public interface RaftMessage
{
	String getSenderId();
	
	long getTerm();
}
