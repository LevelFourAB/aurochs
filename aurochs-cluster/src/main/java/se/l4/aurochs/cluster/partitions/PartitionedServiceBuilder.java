package se.l4.aurochs.cluster.partitions;

import java.util.function.Function;

import se.l4.aurochs.core.channel.ChannelCodec;
import se.l4.commons.io.Bytes;

public interface PartitionedServiceBuilder<T>
{
	/**
	 * Define the coded that should be used for translating messages from
	 * {@link Bytes} into easier to use objects.
	 * 
	 * @param codec
	 * @return
	 */
	<O> PartitionedServiceBuilder<O> withCodec(ChannelCodec<Bytes, O> codec);
	
	/**
	 * Define that this service uses serialization for its messages.
	 *  
	 * @param type
	 * @return
	 */
	<O> PartitionedServiceBuilder<O> withSerializingCodec(Class<O> type);
	
	PartitionChannel<T> create(Function<PartitionCreateEncounter<T>, PartitionService<T>> service);
}
