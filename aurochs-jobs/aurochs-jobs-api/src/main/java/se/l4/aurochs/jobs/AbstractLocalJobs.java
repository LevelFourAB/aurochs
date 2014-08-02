package se.l4.aurochs.jobs;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * Abstract implementation of {@link LocalJobs} that helps with runner registration.
 * 
 * @author Andreas Holstenson
 *
 */
public abstract class AbstractLocalJobs
	implements LocalJobs
{
	private final Map<Class<?>, JobRunner<?>> runners;
	
	public AbstractLocalJobs()
	{
		runners = Maps.newHashMap();
	}
	
	@Override
	public void registerRunner(JobRunner<?> runner)
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public <T> void registerRunner(Class<T> dataType, JobRunner<T> runner)
	{
		if(runners.containsKey(dataType))
		{
			throw new IllegalArgumentException(dataType + " is already registered to " + runners.get(dataType));
		}
		
		runners.put(dataType, runner);
	}
	
	protected JobRunner<?> getRunner(Object data)
	{
		Class<?> type = data.getClass();
		while(type != Object.class)
		{
			JobRunner<?> runner = runners.get(type);
			if(runner != null) return runner;
			
			runner = getRunnerFromInterface(type);
			if(runner != null) return runner;
			
			type = type.getSuperclass();
		}
		
		return null;
	}
	
	private JobRunner<?> getRunnerFromInterface(Class<?> type)
	{
		Class<?>[] interfaces = type.getInterfaces();
		for(Class<?> intf : interfaces)
		{
			JobRunner<?> runner = runners.get(intf);
			if(runner != null) return runner;
		}
		
		for(Class<?> intf : interfaces)
		{
			JobRunner<?> runner = getRunnerFromInterface(intf);
			if(runner != null) return runner;
		}
		
		return null;
	}
}
