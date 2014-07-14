package se.l4.aurochs.cluster.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import se.l4.aurochs.serialization.Serializer;

import com.google.common.collect.Iterators;
import com.hazelcast.core.IQueue;

/**
 * Wrapper for a queue from Hazelcast that uses Aurochs serialization.
 * 
 * @author Andreas Holstenson
 *
 * @param <E>
 */
public class QueueWrapper<E>
	implements BlockingQueue<E>
{
	private final IQueue<byte[]> queue;
	private final Function<E, byte[]> toBytes;
	private final Function<byte[], E> fromBytes;

	public QueueWrapper(IQueue<byte[]> queue, Serializer<E> s)
	{
		this.queue = queue;
		
		toBytes = s.toBytes();
		fromBytes = s.fromBytes();
	}
	
	@SuppressWarnings({ "unchecked" })
	private Collection<byte[]> asBytesUnchecked(@SuppressWarnings("rawtypes") Collection c)
	{
		return asBytes(c);
	}
	
	private Collection<byte[]> asBytes(Collection<? extends E> c)
	{
		return c.stream().map(toBytes).collect(Collectors.toList());
	}
	
	@Override
	public boolean add(E e)
	{
		return queue.add(toBytes.apply(e));
	}
	
	@Override
	public boolean addAll(Collection<? extends E> c)
	{
		return queue.addAll(asBytes(c));
	}

	@Override
	public void clear()
	{
		queue.clear();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean contains(Object o)
	{
		return queue.contains(toBytes.apply((E) o));
	}
	
	@Override
	public boolean containsAll(Collection<?> c)
	{
		return queue.containsAll(asBytesUnchecked(c));
	}
	
	@Override
	public int drainTo(Collection<? super E> c)
	{
		List<byte[]> items = new ArrayList<byte[]>();
		int count = queue.drainTo(items);
		for(int i=0, n=items.size(); i<n; i++)
		{
			c.add(fromBytes.apply(items.get(i)));
		}
		return count;
	}
	
	@Override
	public int drainTo(Collection<? super E> c, int maxElements)
	{
		List<byte[]> items = new ArrayList<byte[]>();
		int count = queue.drainTo(items, maxElements);
		for(int i=0, n=items.size(); i<n; i++)
		{
			c.add(fromBytes.apply(items.get(i)));
		}
		return count;
	}
	
	@Override
	public E element()
	{
		return fromBytes.apply(queue.element());
	}
	
	@Override
	public boolean isEmpty()
	{
		return queue.isEmpty();
	}
	
	@Override
	public Iterator<E> iterator()
	{
		return Iterators.transform(queue.iterator(), (in) -> fromBytes.apply(in));
	}
	
	@Override
	public boolean offer(E e)
	{
		return queue.offer(toBytes.apply(e));
	}
	
	@Override
	public boolean offer(E e, long timeout, TimeUnit unit)
		throws InterruptedException
	{
		return queue.offer(toBytes.apply(e), timeout, unit);
	}
	
	@Override
	public E peek()
	{
		return fromBytes.apply(queue.peek());
	}
	
	@Override
	public E poll()
	{
		return fromBytes.apply(queue.poll());
	}
	
	@Override
	public E poll(long timeout, TimeUnit unit)
		throws InterruptedException
	{
		return fromBytes.apply(queue.poll(timeout, unit));
	}
	
	@Override
	public void put(E e)
		throws InterruptedException
	{
		queue.put(toBytes.apply(e));
	}
	
	@Override
	public int remainingCapacity()
	{
		return queue.remainingCapacity();
	}
	
	@Override
	public E remove()
	{
		return fromBytes.apply(queue.remove());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object o)
	{
		return queue.remove(toBytes.apply((E) o));
	}
	
	@Override
	public boolean removeAll(Collection<?> c)
	{
		return queue.removeAll(asBytesUnchecked(c));
	}
	
	@Override
	public boolean retainAll(Collection<?> c)
	{
		return queue.retainAll(asBytesUnchecked(c));
	}
	
	@Override
	public int size()
	{
		return queue.size();
	}
	
	@Override
	public E take()
		throws InterruptedException
	{
		return fromBytes.apply(queue.take());
	}
	
	@Override
	public Object[] toArray()
	{
		Object[] object = queue.toArray();
		Object[] result = new Object[object.length];
		for(int i=0, n=result.length; i<n; i++)
		{
			result[i] = fromBytes.apply((byte[]) object[i]);
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Object> T[] toArray(T[] a)
	{
		Object[] object = queue.toArray();
		T[] result = a.length < object.length ? Arrays.copyOf(a, object.length) : a;
		for(int i=0, n=result.length; i<n; i++)
		{
			result[i] = (T) fromBytes.apply((byte[]) object[i]);
		}
		
		if(result.length > object.length)
		{
			result[object.length] = null;
		}
		
		return result;
	}
	
	@Override
	public int hashCode()
	{
		return queue.hashCode();
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj)
	{
		if(obj instanceof QueueWrapper)
		{
			return queue.equals(((QueueWrapper) obj).queue);
		}
		
		return false;
	}
}
