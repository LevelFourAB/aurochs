package se.l4.aurochs.cluster.nodes;

import java.util.function.Consumer;

import se.l4.aurochs.core.events.EventHandle;

public interface NodeStates<D>
{
	/**
	 * Listen to changes to the node states.
	 * 
	 * @param event
	 * @return
	 */
	EventHandle listen(Consumer<NodeStateEvent<D>> event);
	
	/**
	 * Get the state of the given node.
	 * 
	 * @param node
	 * @return
	 */
	NodeState get(Node<D> node);
	
	/**
	 * Get a random node that is an available state.
	 * 
	 * @return
	 */
	Node<D> random();
}
