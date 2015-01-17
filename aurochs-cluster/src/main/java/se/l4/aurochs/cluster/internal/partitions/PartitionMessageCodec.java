package se.l4.aurochs.cluster.internal.partitions;

import java.io.IOException;

import se.l4.aurochs.core.channel.ChannelCodec;
import se.l4.aurochs.core.io.Bytes;
import se.l4.aurochs.core.io.ExtendedDataInput;

import com.google.common.base.Throwables;

public class PartitionMessageCodec<T>
	implements ChannelCodec<Bytes, PartitionMessage<T>>
{
	private final ChannelCodec<Bytes, T> subCodec;

	public PartitionMessageCodec(ChannelCodec<Bytes, T> subCodec)
	{
		this.subCodec = subCodec;
	}

	@Override
	public boolean accepts(Bytes in)
	{
		return true;
	}

	@Override
	public PartitionMessage<T> fromSource(Bytes object)
	{
		try(ExtendedDataInput in = object.asDataInput())
		{
			int partition = in.readVInt();
			T payload = subCodec.fromSource(in.readBytes());
			
			return new PartitionMessage<T>(partition, payload);
		}
		catch(IOException e)
		{
			throw Throwables.propagate(e);
		}
	}

	@Override
	public Bytes toSource(PartitionMessage<T> object)
	{
		return Bytes.create(out -> {
			out.writeVInt(object.getPartition());
			out.writeBytes(subCodec.toSource(object.getPayload()));
		});
	}

}
