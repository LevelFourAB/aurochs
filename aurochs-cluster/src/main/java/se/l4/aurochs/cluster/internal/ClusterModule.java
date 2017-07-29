package se.l4.aurochs.cluster.internal;

import com.google.inject.name.Named;

import se.l4.aurochs.cluster.Cluster;
import se.l4.aurochs.core.AutoLoad;
import se.l4.crayon.Contribution;
import se.l4.crayon.CrayonModule;
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
	@Named("cluster")
	public void provideClusterService(ServiceManager manager, ClusterImpl cluster)
	{
		manager.addService(cluster);
	}
}
