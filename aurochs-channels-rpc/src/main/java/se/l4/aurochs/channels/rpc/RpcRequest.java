package se.l4.aurochs.channels.rpc;
public interface RpcRequest<T>
	extends RpcMessage<T>
{
	T getPayload();
}
