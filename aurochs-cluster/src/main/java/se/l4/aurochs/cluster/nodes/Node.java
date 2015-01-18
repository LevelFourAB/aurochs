package se.l4.aurochs.cluster.nodes;

import se.l4.aurochs.core.channel.Channel;
import se.l4.aurochs.core.channel.ChannelCodec;


public class Node<T>
{
	private final String id;
	
	private final Channel<T> controlIncoming;
	private final Channel<T> controlOutgoing;
	
	private final Channel<T> incoming;
	private final Channel<T> outgoing;
	
	public Node(String id,
			Channel<T> controlIncoming, Channel<T> controlOutgoing,
			Channel<T> incoming, Channel<T> outgoing)
	{
		this.id = id;
		
		this.controlIncoming = controlIncoming;
		this.controlOutgoing = controlOutgoing;
		
		this.incoming = incoming;
		this.outgoing = outgoing;
	}
	
	public String getId()
	{
		return id;
	}
	
	public Channel<T> controlIncoming()
	{
		return controlIncoming;
	}
	
	public Channel<T> controlOutgoing()
	{
		return controlOutgoing;
	}
	
	public Channel<T> incoming()
	{
		return incoming;
	}
	
	public Channel<T> outgoing()
	{
		return outgoing;
	}
	
	public <O> Node<O> transform(ChannelCodec<T, O> codec)
	{
		Channel<O> incoming;
		Channel<O> outgoing;
		if(this.incoming == this.outgoing)
		{
			incoming = outgoing = this.incoming.transform(codec);
		}
		else
		{
			incoming = this.incoming.transform(codec);
			outgoing = this.outgoing.transform(codec);
		}
		
		Channel<O> controlIncoming;
		Channel<O> controlOutgoing;
		if(this.controlIncoming == this.controlOutgoing)
		{
			controlIncoming = controlOutgoing = this.controlIncoming.transform(codec);
		}
		else
		{
			controlIncoming = this.controlIncoming.transform(codec);
			controlOutgoing = this.controlOutgoing.transform(codec);
		}
		
		return new Node<>(id, controlIncoming, controlOutgoing, incoming, outgoing);
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
