package se.l4.aurochs.cluster.internal.partitions;

import java.util.function.Consumer;
import java.util.function.Function;

import se.l4.aurochs.cluster.partitions.PartitionChannel;
import se.l4.aurochs.cluster.partitions.PartitionCreateEncounter;
import se.l4.aurochs.cluster.partitions.PartitionService;
import se.l4.aurochs.cluster.partitions.PartitionedServiceBuilder;
import se.l4.aurochs.core.channel.ChannelCodec;
import se.l4.aurochs.core.channel.SerializationCodec;
import se.l4.aurochs.core.channel.rpc.RPCChannelCodec;
import se.l4.aurochs.core.channel.rpc.RpcMessage;
import se.l4.aurochs.core.internal.NamedChannelCodec;
import se.l4.aurochs.core.io.ByteMessage;
import se.l4.aurochs.core.io.Bytes;
import se.l4.commons.serialization.SerializerCollection;

public class PartitionedServiceBuilderImpl<T>
	implements PartitionedServiceBuilder<T>
{
	private final SerializerCollection serializers;
	private final String name;
	private final Partitions<ByteMessage> partitions;
	private final Consumer<PartitionServiceRegistration<T>> finisher;
	
	private ChannelCodec<Bytes, T> codec;

	public PartitionedServiceBuilderImpl(SerializerCollection serializers, Partitions<ByteMessage> partitions, String name,
			Consumer<PartitionServiceRegistration<T>> finisher)
	{
		this.serializers = serializers;
		this.partitions = partitions;
		this.name = name;
		this.finisher = finisher;
	}

	@Override
	public <O> PartitionedServiceBuilder<O> withCodec(ChannelCodec<Bytes, O> codec)
	{
		this.codec = (ChannelCodec) codec;
		return (PartitionedServiceBuilder) this;
	}

	@Override
	public <O> PartitionedServiceBuilder<O> withSerializingCodec(Class<O> type)
	{
		return withCodec(SerializationCodec.newDynamicCodec(serializers, type));
	}

	@Override
	public PartitionChannel<T> create(Function<PartitionCreateEncounter<T>, PartitionService<T>> service)
	{
		if(codec == null) throw new IllegalArgumentException("A ChannelCodec must be specified");
		
		ChannelCodec<ByteMessage, RpcMessage<PartitionMessage<T>>> channelCodec = new NamedChannelCodec("service:" + name)
			.then(new RPCChannelCodec<>(new PartitionMessageCodec<>(codec)));
		
		ServicePartitionChannel<T> channel = new ServicePartitionChannel<>(
			new TransformedPartitions<>(partitions, channelCodec)
		);
		
		finisher.accept(new PartitionServiceRegistration<>(name, channel, service));
		
		return channel;
	}
	
}
