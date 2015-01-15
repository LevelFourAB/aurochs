package se.l4.aurochs.cluster.def;

import se.l4.aurochs.cluster.Cluster;
import se.l4.aurochs.cluster.def.lock.ClusterLockHandler;
import se.l4.aurochs.cluster.def.lock.LockMessage;
import se.l4.aurochs.cluster.partitions.PartitionChannel;
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
	
	@Singleton
	@Provides
	public PartitionChannel<LockMessage> provideLockChannel(Cluster cluster)
	{
		return cluster.newService("aurochs:lock")
			.partitioned()
			.withSerializingCodec(LockMessage.class)
			.create(ClusterLockHandler::new);
	}
}
