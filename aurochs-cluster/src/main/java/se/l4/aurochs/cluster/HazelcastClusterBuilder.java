package se.l4.aurochs.cluster;

import java.util.List;
import java.util.Map;

import se.l4.aurochs.cluster.internal.ClusterConfig;
import se.l4.aurochs.cluster.internal.ClusterConfig.MulticastNetworkConfig;
import se.l4.aurochs.cluster.internal.ClusterConfig.StaticNetworkConfig;
import se.l4.aurochs.cluster.internal.HazelcastClusterImpl;
import se.l4.aurochs.config.Config;
import se.l4.aurochs.config.ConfigException;
import se.l4.aurochs.serialization.SerializerCollection;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.InterfacesConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.SerializationConfig;
import com.hazelcast.config.ServiceConfig;
import com.hazelcast.config.ServicesConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.nio.serialization.PortableHook;
import com.hazelcast.spi.ManagedService;


/**
 * Build a new cluster based on Hazelcast.
 * 
 * @author Andreas Holstenson
 *
 */
public class HazelcastClusterBuilder
{
	private final Injector injector;
	private final SerializerCollection serializers;
	private final Config config;
	
	private final Map<String, ManagedService> services;
	
	private ClusterConfig clusterConfig;

	@Inject
	public HazelcastClusterBuilder(Injector injector, SerializerCollection serializers, Config config)
	{
		this.injector = injector;
		this.serializers = serializers;
		this.config = config;
		
		services = Maps.newHashMap();
		clusterConfig = new ClusterConfig();
	}
	
	/**
	 * Build this cluster from a configuration found at the given key.
	 * 
	 * @param key
	 * @return
	 */
	public HazelcastClusterBuilder fromConfig(String key)
	{
		ClusterConfig cc = config.asObject(key, ClusterConfig.class);
		if(cc == null)
		{
			throw new ConfigException("No cluster config found for " + key);
		}
		this.clusterConfig = cc;
		return this;
	}
	
	/**
	 * Tell the cluster to make a {@link ManagedService} available. This is advanced usage
	 * and intended to provide services that run on top of Hazelcast.
	 * 
	 * @param id
	 * @param type
	 * @return
	 */
	public HazelcastClusterBuilder withService(String id, ManagedService service)
	{
		services.put(id, service);
		
		return this;
	}
	
	public HazelcastClusterBuilder withPartitions(int partitions)
	{
		clusterConfig.setPartitions(partitions);
		
		return this;
	}
	
	public HazelcastInstance buildInstance()
	{
		List<PortableHook> factories = Lists.newArrayList();
		injector.getBindings().forEach((key, value) -> {
			if(PortableHook.class.isAssignableFrom(key.getTypeLiteral().getRawType()))
			{
				factories.add((PortableHook) value.getProvider().get());
			}
		});
		
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
			
			return HazelcastClient.newHazelcastClient(config);
		}
		else
		{
			com.hazelcast.config.Config config = new com.hazelcast.config.Config();
			
			config.setProperty("hazelcast.partition.count", String.valueOf(clusterConfig.getPartitions()));
			
			NetworkConfig nc = new NetworkConfig();
			nc.setPort(clusterConfig.getPort());
			nc.setPortAutoIncrement(true);
			config.setNetworkConfig(nc);
			
			JoinConfig j = new JoinConfig();
			j.setMulticastConfig(toMulticast(clusterConfig.getMulticast()));
			j.setTcpIpConfig(toTcpIp(clusterConfig.getStaticNetwork()));
			nc.setJoin(j);
			
			if(clusterConfig.getInterfaces() != null)
			{
				InterfacesConfig ic = nc.getInterfaces();
				ic.setEnabled(true);
				ic.clear();
				for(String c : clusterConfig.getInterfaces())
				{
					ic.addInterface(c);
				}
			}
			
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
			
			return Hazelcast.newHazelcastInstance(config);
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
	
	public HazelcastCluster build()
	{
		return new HazelcastClusterImpl(serializers, buildInstance());
	}
}
