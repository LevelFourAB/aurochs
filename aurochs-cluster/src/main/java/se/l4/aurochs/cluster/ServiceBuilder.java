package se.l4.aurochs.cluster;

import se.l4.aurochs.cluster.partitions.PartitionMessage;
import se.l4.aurochs.cluster.partitions.PartitionedServiceBuilder;
import se.l4.aurochs.core.channel.ChannelCodec;
import se.l4.aurochs.core.channel.ChannelListener;
import se.l4.aurochs.core.io.Bytes;

public interface ServiceBuilder<T>
{
	PartitionedServiceBuilder<PartitionMessage> partitioned();
	
	<O> ServiceBuilder<O> withCodec(ChannelCodec<Bytes, O> codec);
	
	ServiceBuilder<T> withMessageHandler(ChannelListener<T> listener);
}
