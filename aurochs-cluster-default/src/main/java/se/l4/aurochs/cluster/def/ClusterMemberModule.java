package se.l4.aurochs.cluster.def;

import se.l4.aurochs.cluster.Cluster;
import se.l4.aurochs.cluster.HazelcastClusterBuilder;
import se.l4.aurochs.core.AutoLoad;
import se.l4.crayon.CrayonModule;

import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * This module will automatically join a cluster at startup. The cluster is defined via the
 * config key {@code cluster}.
 * 
 * @author Andreas Holstenson
 *
 */
@AutoLoad
public class ClusterMemberModule
	extends CrayonModule
{
	@Override
	protected void configure()
	{
	}
	
	@Provides
	@Singleton
	public Cluster provideCluster(HazelcastClusterBuilder builder)
	{
		return builder.fromConfig("cluster").build();
	}
}
