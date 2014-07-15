package se.l4.aurochs.cluster.internal;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;

import se.l4.aurochs.cluster.Cluster;
import se.l4.aurochs.cluster.HazelcastCluster;
import se.l4.aurochs.cluster.internal.ClusterConfig.MulticastNetworkConfig;
import se.l4.aurochs.cluster.internal.ClusterConfig.StaticNetworkConfig;
import se.l4.aurochs.config.ConfigException;
import se.l4.aurochs.core.channel.Channel;
import se.l4.aurochs.serialization.SerializerCollection;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.SerializationConfig;
import com.hazelcast.config.ServiceConfig;
import com.hazelcast.config.ServicesConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ITopic;
import com.hazelcast.nio.serialization.PortableHook;
import com.hazelcast.spi.ManagedService;

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
			ClusterConfig clusterConfig,
			Map<String, ManagedService> services,
			List<PortableHook> factories)
	{
		this.serializers = serializers;
		
		if(clusterConfig.isClient())
		{
			ClientConfig config = new ClientConfig();
			
			if(clusterConfig.getStaticNetwork() == null)
			{
				throw new ConfigException("Need to use static addresses if joining cluster as a client");
			}
			
			config.setAddresses(clusterConfig.getStaticNetwork().getHosts());
			
			updateFactories(config.getSerializationConfig(), factories);

			// TODO: Partitions?
			// TODO: Services?
			
			hazelcast = HazelcastClient.newHazelcastClient(config);
		}
		else
		{
			Config config = new Config();
			
			config.setProperty("hazelcast.partition.count", String.valueOf(clusterConfig.getPartitions()));
			
			NetworkConfig nc = new NetworkConfig();
			nc.setPort(clusterConfig.getPort());
			nc.setPortAutoIncrement(true);
			config.setNetworkConfig(nc);
			
			JoinConfig j = new JoinConfig();
			j.setMulticastConfig(toMulticast(clusterConfig.getMulticast()));
			j.setTcpIpConfig(toTcpIp(clusterConfig.getStaticNetwork()));
			nc.setJoin(j);
			
			ServicesConfig scs = config.getServicesConfig();
			for(Map.Entry<String, ManagedService> e : services.entrySet())
			{
				ServiceConfig sc = new ServiceConfig();
				sc.setEnabled(true);
				sc.setName(e.getKey());
				sc.setServiceImpl(e.getValue());
				scs.addServiceConfig(sc);
			}
			
			updateFactories(config.getSerializationConfig(), factories);
			
			hazelcast = Hazelcast.newHazelcastInstance(config);
		}
	}
	
	private void updateFactories(SerializationConfig serializationConfig,
			List<PortableHook> factories)
	{
		for(PortableHook ph : factories)
		{
			serializationConfig.addPortableFactory(ph.getFactoryId(), ph.createFactory());
		}
	}

	private TcpIpConfig toTcpIp(StaticNetworkConfig config)
	{
		if(config == null)
		{
			return new TcpIpConfig();
		}
		
		TcpIpConfig tc = new TcpIpConfig();
		tc.setEnabled(true);
		for(String s : config.getHosts())
		{
			tc.addMember(s);
		}
		return tc;
	}

	private MulticastConfig toMulticast(MulticastNetworkConfig config)
	{
		if(config == null)
		{
			MulticastConfig mc = new MulticastConfig();
			mc.setEnabled(false);
			return mc;
		}
		
		MulticastConfig mc = new MulticastConfig();
		mc.setMulticastGroup(config.getGroup());
		mc.setMulticastPort(config.getPort());
		
		return mc;
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
