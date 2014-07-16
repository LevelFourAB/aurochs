package se.l4.aurochs.cluster.internal;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;

import se.l4.aurochs.cluster.Cluster;
import se.l4.aurochs.cluster.HazelcastCluster;
import se.l4.aurochs.core.channel.Channel;
import se.l4.aurochs.serialization.SerializerCollection;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ITopic;

/**
 * Implementation of {@link Cluster} using {@link Hazelcast}.
 * 
 * @author Andreas Holstenson
 *
 */
public class HazelcastClusterImpl
	implements HazelcastCluster
{
	private final HazelcastInstance hazelcast;
	private final SerializerCollection serializers;
	
	public HazelcastClusterImpl(SerializerCollection serializers,
			HazelcastInstance instance)
	{
		this.serializers = serializers;
		this.hazelcast = instance;
	}
	
	@Override
	public HazelcastInstance getInstance()
	{
		return hazelcast;
	}

	@Override
	public <T> BlockingQueue<T> getQueue(String name, Class<T> type)
	{
		IQueue<byte[]> queue = hazelcast.getQueue(name);
		return new QueueWrapper<T>(queue, serializers.find(type));
	}
	
	@Override
	public <T> Channel<T> getChannel(String name, Class<T> type)
	{
		ITopic<byte[]> topic = hazelcast.getTopic(name);
		return new TopicAsChannel<T>(topic, serializers.find(type));
	}
	
	@Override
	public Lock getLock(String name)
	{
		return hazelcast.getLock(name);
	}
}
