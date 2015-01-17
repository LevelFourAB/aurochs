package se.l4.aurochs.cluster.nodes;

import se.l4.aurochs.core.channel.Channel;


public class Node<T>
{
	private final String id;
	private final Channel<T> incoming;
	private final Channel<T> outgoing;
	
	public Node(String id, Channel<T> incoming, Channel<T> outgoing)
	{
		this.id = id;
		this.incoming = incoming;
		this.outgoing = outgoing;
	}
	
	public String getId()
	{
		return id;
	}
	
	public Channel<T> incoming()
	{
		return incoming;
	}
	
	public Channel<T> outgoing()
	{
		return outgoing;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if(id == null)
		{
			if(other.id != null)
				return false;
		}
		else if(!id.equals(other.id))
			return false;
		return true;
	}
}
