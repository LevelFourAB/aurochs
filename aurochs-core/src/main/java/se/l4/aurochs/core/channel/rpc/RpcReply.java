package se.l4.aurochs.core.channel.rpc;

public interface RpcReply<T>
	extends RpcMessage<T>
{
	/**
	 * Get the payload to return.
	 * 
	 * @return
	 */
	T getPayload();
}
