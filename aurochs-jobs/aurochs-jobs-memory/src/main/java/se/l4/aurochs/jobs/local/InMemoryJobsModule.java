package se.l4.aurochs.jobs.local;

import se.l4.aurochs.core.AutoLoad;
import se.l4.aurochs.jobs.Jobs;
import se.l4.aurochs.jobs.LocalJobs;
import se.l4.crayon.CrayonModule;

@AutoLoad
public class InMemoryJobsModule
	extends CrayonModule
{

	@Override
	protected void configure()
	{
		bind(LocalJobs.class).to(LocalJobsImpl.class);
		bind(Jobs.class).to(LocalJobsImpl.class);
	}

}
