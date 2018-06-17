package se.l4.aurochs.channels.rpc;

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
