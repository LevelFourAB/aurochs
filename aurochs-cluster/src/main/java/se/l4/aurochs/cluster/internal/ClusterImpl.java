package se.l4.aurochs.cluster.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.l4.aurochs.cluster.Cluster;
import se.l4.aurochs.cluster.ServiceBuilder;
import se.l4.aurochs.config.Config;
import se.l4.aurochs.core.io.Bytes;
import se.l4.crayon.services.ManagedService;

import com.google.inject.Inject;

public class ClusterImpl
	implements Cluster, ManagedService
{
	private static final Logger log = LoggerFactory.getLogger(Cluster.class);
	
	private ClusterConfig config;

	@Inject
	public ClusterImpl(Config config)
	{
		this.config = config.get("cluster", ClusterConfig.class)
			.getOrDefault(new ClusterConfig());
	}
	
	@Override
	public void start()
		throws Exception
	{
		switch(config.getType())
		{
			case NONE:
				log.info("Not joining any cluster");
				return;
			case SERVER:
				log.info("Joining cluster as a full server member");
				break;
			case CLIENT:
				log.warn("Joining as a client not yet supported");
				break;
		}
	}
	
	@Override
	public void stop()
		throws Exception
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public MemberType getLocalType()
	{
		return config.getType();
	}
	
	@Override
	public ServiceBuilder<Bytes> newService(String name)
	{
		// TODO Auto-generated method stub
		return null;
	}
}
