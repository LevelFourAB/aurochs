package se.l4.aurochs.cluster.internal.service;

import se.l4.aurochs.cluster.ServiceBuilder;
import se.l4.aurochs.cluster.internal.partitions.LocalPartitionsCoordinator;
import se.l4.aurochs.cluster.partitions.PartitionMessage;
import se.l4.aurochs.cluster.partitions.PartitionedServiceBuilder;
import se.l4.aurochs.core.channel.ChannelCodec;
import se.l4.aurochs.core.channel.ChannelListener;
import se.l4.commons.io.Bytes;

public class ServiceBuilderImpl<T>
	implements ServiceBuilder<T>
{
	private final String name;
	private final LocalPartitionsCoordinator partitionCoordinator;

	public ServiceBuilderImpl(String name, LocalPartitionsCoordinator partitionCoordinator)
	{
		this.name = name;
		this.partitionCoordinator = partitionCoordinator;
	}

	@Override
	public PartitionedServiceBuilder<PartitionMessage> partitioned()
	{
		return partitionCoordinator.newBuilder(name);
	}

	@Override
	public <O> ServiceBuilder<O> withCodec(ChannelCodec<Bytes, O> codec)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public ServiceBuilder<T> withMessageHandler(ChannelListener<T> listener)
	{
		throw new UnsupportedOperationException();
	}

}
