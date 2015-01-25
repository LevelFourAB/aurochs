package se.l4.aurochs.cluster.internal.partitions;

import java.util.function.Function;

import se.l4.aurochs.cluster.partitions.PartitionCreateEncounter;
import se.l4.aurochs.cluster.partitions.PartitionService;

public class PartitionServiceRegistration<T>
{
	private final String name;
	private final ServicePartitionChannel<T> channel;
	private final Function<PartitionCreateEncounter<T>, PartitionService<T>> factory;

	public PartitionServiceRegistration(String name, ServicePartitionChannel<T> channel, Function<PartitionCreateEncounter<T>, PartitionService<T>> factory)
	{
		this.name = name;
		this.channel = channel;
		this.factory = factory;
	}
	
	public String getName()
	{
		return name;
	}
	
	public ServicePartitionChannel<T> getChannel()
	{
		return channel;
	}
	
	public Function<PartitionCreateEncounter<T>, PartitionService<T>> getFactory()
	{
		return factory;
	}
}
