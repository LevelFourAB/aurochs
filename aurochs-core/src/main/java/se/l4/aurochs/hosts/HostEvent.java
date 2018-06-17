package se.l4.aurochs.hosts;

import java.net.URI;

public class HostEvent
{
	public enum Type
	{
		INITIAL,
		ADDED,
		REMOVED
	}
	
	private final Type type;
	private final URI uri;
	
	public HostEvent(Type type, URI uri)
	{
		this.type = type;
		this.uri = uri;
	}
	
	public Type getType()
	{
		return type;
	}
	
	public URI getUri()
	{
		return uri;
	}
}
