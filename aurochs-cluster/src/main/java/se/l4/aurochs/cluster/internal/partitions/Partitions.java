package se.l4.aurochs.cluster.internal.partitions;

import java.util.function.Consumer;

import se.l4.aurochs.cluster.nodes.Node;
import se.l4.aurochs.cluster.nodes.NodeStates;
import se.l4.aurochs.cluster.nodes.NodeSet;
import se.l4.aurochs.core.events.EventHandle;

public interface Partitions<T>
{
	/**
	 * Get the local node.
	 * 
	 * @return
	 */
	Node<T> getLocal();
	
	/**
	 * Get the total number of partitions.
	 * 
	 * @return
	 */
	int getTotal();
	
	/**
	 * Listen for partitioning changes.
	 * 
	 * @param listener
	 * @return
	 */
	EventHandle listen(Consumer<PartitionEvent<T>> listener);
	
	/**
	 * Get all the nodes in the given partition.
	 * 
	 * @param partition
	 * @return
	 */
	NodeSet<T> getNodes(int partition);
	
	/**
	 * Get all the nodes (with state) in the given partition.
	 * 
	 * @param partition
	 * @return
	 */
	NodeStates<T> getNodeStates(int partition);
	
	/**
	 * Fetch all nodes (with possible duplicates).
	 * 
	 * @return
	 */
	Iterable<Node<T>> listNodes();
	
	/**
	 * Perform an action for one of the nodes in the given partition.
	 * 
	 * @param partition
	 * @param action
	 */
	void forOneIn(int partition, Consumer<Node<T>> action);
	
	/**
	 * Perform an action for all of the nodes in a given partition.
	 * 
	 * @param partition
	 * @param action
	 */
	void forAllIn(int partition, Consumer<Node<T>> action);
}
