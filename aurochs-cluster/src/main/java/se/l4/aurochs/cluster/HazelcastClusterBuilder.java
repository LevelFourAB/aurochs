package se.l4.aurochs.cluster;

import java.util.List;
import java.util.Map;

import se.l4.aurochs.cluster.internal.ClusterConfig;
import se.l4.aurochs.cluster.internal.HazelcastClusterImpl;
import se.l4.aurochs.config.Config;
import se.l4.aurochs.serialization.SerializerCollection;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Injector;
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
	
	public HazelcastCluster build()
	{
		List<PortableHook> factories = Lists.newArrayList();
		injector.getBindings().forEach((key, value) -> {
			if(PortableHook.class.isAssignableFrom(key.getTypeLiteral().getRawType()))
			{
				factories.add((PortableHook) value.getProvider().get());
			}
		});
		
		return new HazelcastClusterImpl(serializers, clusterConfig, services, factories);
	}
}
