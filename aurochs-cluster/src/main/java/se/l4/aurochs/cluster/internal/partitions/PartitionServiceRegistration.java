package se.l4.aurochs.cluster.internal.partitions;

import java.util.function.Function;

import se.l4.aurochs.cluster.partitions.PartitionChannel;
import se.l4.aurochs.cluster.partitions.PartitionCreateEncounter;
import se.l4.aurochs.cluster.partitions.PartitionMessage;
import se.l4.aurochs.cluster.partitions.PartitionService;

public class PartitionServiceRegistration<T extends PartitionMessage>
{
	private final String name;
	private final PartitionChannel<T> channel;
	private final Function<PartitionCreateEncounter<T>, PartitionService<T>> factory;

	public PartitionServiceRegistration(String name, PartitionChannel<T> channel, Function<PartitionCreateEncounter<T>, PartitionService<T>> factory)
	{
		this.name = name;
		this.channel = channel;
		this.factory = factory;
	}
	
	public String getName()
	{
		return name;
	}
	
	public PartitionChannel<T> getChannel()
	{
		return channel;
	}
	
	public Function<PartitionCreateEncounter<T>, PartitionService<T>> getFactory()
	{
		return factory;
	}
}
