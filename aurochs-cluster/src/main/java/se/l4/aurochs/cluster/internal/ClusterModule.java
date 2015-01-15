package se.l4.aurochs.cluster.internal;

import se.l4.aurochs.cluster.Cluster;
import se.l4.aurochs.core.AutoLoad;
import se.l4.crayon.CrayonModule;
import se.l4.crayon.annotation.Contribution;
import se.l4.crayon.services.ServiceManager;

@AutoLoad
public class ClusterModule
	extends CrayonModule
{
	@Override
	protected void configure()
	{
		bind(Cluster.class).to(ClusterImpl.class);
	}
	
	@Contribution
	public void provideClusterService(ServiceManager manager, ClusterImpl cluster)
	{
		manager.addService(cluster);
	}
}