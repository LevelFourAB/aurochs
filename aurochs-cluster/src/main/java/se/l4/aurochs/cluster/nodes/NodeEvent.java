package se.l4.aurochs.cluster.nodes;

public class NodeEvent<T>
{
	public enum Type
	{
		INITIAL,
		ADDED,
		REMOVED
	}
	
	private final Type type;
	private final Node<T> node;
	
	public NodeEvent(Type type, Node<T> node)
	{
		this.type = type;
		this.node = node;
	}
	
	public Type getType()
	{
		return type;
	}
	
	public Node<T> getNode()
	{
		return node;
	}
}
