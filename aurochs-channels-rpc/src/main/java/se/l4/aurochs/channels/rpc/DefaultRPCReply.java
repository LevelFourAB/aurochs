package se.l4.aurochs.channels.rpc;

public class DefaultRPCReply<T>
	implements RpcReply<T>
{
	private final long id;
	private final T payload;
	
	public DefaultRPCReply(long id, T payload)
	{
		this.id = id;
		this.payload = payload;
	}
	
	@Override
	public long getRequestId()
	{
		return id;
	}

	@Override
	public T getPayload()
	{
		return payload;
	}
}
