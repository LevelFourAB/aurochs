package se.l4.aurochs.cluster.internal.raft;

import se.l4.aurochs.core.channel.Channel;


public class Node<T>
{
	private final String id;
	private final Channel<T> channel;
	
	public Node(String id, Channel<T> channel)
	{
		this.id = id;
		this.channel = channel;
	}
	
	public String getId()
	{
		return id;
	}
	
	public Channel<T> getChannel()
	{
		return channel;
	}
}
