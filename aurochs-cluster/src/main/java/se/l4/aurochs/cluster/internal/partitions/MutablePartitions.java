package se.l4.aurochs.cluster.internal.partitions;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import se.l4.aurochs.cluster.internal.MutableNodes;
import se.l4.aurochs.cluster.nodes.Node;
import se.l4.aurochs.cluster.nodes.Nodes;
import se.l4.aurochs.core.events.EventHandle;
import se.l4.aurochs.core.events.EventHelper;

import com.google.common.collect.Iterables;

public class MutablePartitions<T>
	implements Partitions<T>
{
	private final EventHelper<Consumer<PartitionEvent<T>>> events;
	
	private final Lock lock;
	private final MutableNodes<T>[] partitionToNode;
	private final Set<NodeInfo<T>> nodes;

	@SuppressWarnings("unchecked")
	public MutablePartitions(int total)
	{
		events = new EventHelper<>();
		
		lock = new ReentrantLock();
		nodes = new CopyOnWriteArraySet<>();
		partitionToNode = new MutableNodes[total];
		for(int i=0; i<total; i++)
		{
			partitionToNode[i] = new MutableNodes<>();
		}
	}
	
	@Override
	public int getTotal()
	{
		return partitionToNode.length;
	}
	
	public void join(int partition, Node<T> node)
	{
		lock.lock();
		try
		{
			if(nodes.add(new NodeInfo<T>(partition, node)))
			{
				partitionToNode[partition].addNode(node);
			}
		}
		finally
		{
			lock.unlock();
		}
	}
	
	public void leave(int partition, Node<T> node)
	{
		lock.lock();
		try
		{
			if(nodes.remove(new NodeInfo<T>(partition, node)))
			{
				partitionToNode[partition].removeNode(node);
			}
		}
		finally
		{
			lock.unlock();
		}
	}
	
	@Override
	public EventHandle listen(Consumer<PartitionEvent<T>> listener)
	{
		for(NodeInfo<T> i : nodes)
		{
			listener.accept(new PartitionEvent<>(PartitionEvent.Type.JOINED, i.partition, i.node));
		}
		
		return events.listen(listener);
	}
	
	@Override
	public Iterable<Node<T>> listNodes()
	{
		return Iterables.transform(nodes, in -> in.node);
	}
	
	@Override
	public Nodes<T> getNodes(int partition)
	{
		lock.lock();
		try
		{
			return partitionToNode[partition];
		}
		finally
		{
			lock.unlock();
		}
	}
	
	@Override
	public void forAllIn(int partition, Consumer<Node<T>> action)
	{
		MutableNodes<T> nodes;
		lock.lock();
		try
		{
			nodes = partitionToNode[partition];
		}
		finally
		{
			lock.unlock();
		}
		
		for(Node<T> n : nodes.list())
		{
			action.accept(n);
		}
	}
	
	@Override
	public void forOneIn(int partition, Consumer<Node<T>> action)
	{
		MutableNodes<T> nodes;
		lock.lock();
		try
		{
			nodes = partitionToNode[partition];
		}
		finally
		{
			lock.unlock();
		}
		
		Collection<Node<T>> all = nodes.list();
		if(all.isEmpty()) return;
		
		int select = ThreadLocalRandom.current().nextInt(all.size());
		Iterator<Node<T>> it = all.iterator();
		for(int i=0; i<select; i++) it.next();
		
		action.accept(it.next());
	}
	
	private static class NodeInfo<T>
	{
		private final int partition;
		private final Node<T> node;
		
		public NodeInfo(int partition, Node<T> node)
		{
			this.partition = partition;
			this.node = node;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((node == null) ? 0 : node.hashCode());
			result = prime * result + partition;
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			NodeInfo other = (NodeInfo) obj;
			if (node == null)
			{
				if (other.node != null)
					return false;
			}
			else if (!node.equals(other.node))
				return false;
			if (partition != other.partition)
				return false;
			return true;
		}
	}
}
