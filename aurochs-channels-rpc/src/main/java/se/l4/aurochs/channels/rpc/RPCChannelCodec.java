package se.l4.aurochs.channels.rpc;

import java.io.IOException;

import com.google.common.base.Throwables;

import se.l4.aurochs.channels.ChannelCodec;
import se.l4.commons.io.Bytes;
import se.l4.commons.io.ExtendedDataInput;

public class RPCChannelCodec<T>
	implements ChannelCodec<Bytes, RpcMessage<T>>
{
	private final ChannelCodec<Bytes, T> subCodec;

	public RPCChannelCodec(ChannelCodec<Bytes, T> subCodec)
	{
		this.subCodec = subCodec;
	}
	
	@Override
	public boolean accepts(Bytes in)
	{
		return true;
	}
	
	@Override
	public RpcMessage<T> fromSource(Bytes object)
	{
		try(ExtendedDataInput in = object.asDataInput())
		{
			int tag = in.readUnsignedByte();
			long id = in.readVLong();
			T payload = subCodec.fromSource(in.readTemporaryBytes());
			switch(tag)
			{
				case 0:
					return new DefaultRPCRequest<T>(id, payload);
				case 1:
					return new DefaultRPCReply<T>(id, payload);
				default:
					throw new RuntimeException("Unknown RPC message: " + tag);
			}
		}
		catch(IOException e)
		{
			throw Throwables.propagate(e);
		}
	}
	
	@Override
	public Bytes toSource(RpcMessage<T> object)
	{
		return Bytes.lazyViaDataOutput(out -> {
			if(object instanceof RpcRequest)
			{
				RpcRequest<T> req = (RpcRequest<T>) object;
				out.write(0);
				out.writeVLong(req.getRequestId());
				out.writeBytes(subCodec.toSource(req.getPayload()));
			}
			else if(object instanceof RpcReply)
			{
				RpcReply<T> req = (RpcReply<T>) object;
				out.write(1);
				out.writeVLong(req.getRequestId());
				out.writeBytes(subCodec.toSource(req.getPayload()));
			}
			else
			{
				throw new RuntimeException("Unknown RPC message: " + object);
			}
		});
	}
}
