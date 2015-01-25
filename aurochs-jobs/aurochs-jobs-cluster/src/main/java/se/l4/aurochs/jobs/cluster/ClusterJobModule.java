package se.l4.aurochs.jobs.cluster;

import se.l4.aurochs.core.AutoLoad;
import se.l4.aurochs.jobs.Jobs;
import se.l4.aurochs.jobs.LocalJobs;
import se.l4.crayon.CrayonModule;
import se.l4.crayon.annotation.Contribution;

@AutoLoad
public class ClusterJobModule
	extends CrayonModule
{

	@Override
	protected void configure()
	{
		bind(Jobs.class).to(JobsImpl.class);
		bind(LocalJobs.class).to(JobsImpl.class);
	}
	
	@Contribution
	public void forceStart(Jobs jobs)
	{
	}
}
