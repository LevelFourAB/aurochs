package se.l4.aurochs.core.channel.rpc;

public interface RpcRequest<T>
	extends RpcMessage<T>
{
	T getPayload();
}
