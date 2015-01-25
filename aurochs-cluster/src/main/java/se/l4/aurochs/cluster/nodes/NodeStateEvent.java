package se.l4.aurochs.cluster.nodes;

public class NodeStateEvent<D>
{
	private final Node<D> node;
	private final NodeState state;

	public NodeStateEvent(Node<D> node, NodeState state)
	{
		this.node = node;
		this.state = state;
	}
	
	public Node<D> getNode()
	{
		return node;
	}
	
	public NodeState getState()
	{
		return state;
	}
}
