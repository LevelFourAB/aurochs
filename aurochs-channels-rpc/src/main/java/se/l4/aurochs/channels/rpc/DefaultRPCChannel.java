package se.l4.aurochs.channels.rpc;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import se.l4.aurochs.channels.Channel;

public class DefaultRPCChannel<T>
	implements RPCChannel<T>
{
	private final Channel<RpcMessage<T>> parent;
	private final RPCHelper<T> helper;

	public DefaultRPCChannel(Channel<RpcMessage<T>> parent, Function<T, CompletableFuture<T>> requestHandler)
	{
		this.parent = parent;
		
		helper = RPC.createHelper(requestHandler);
		parent.addMessageListener(e -> helper.accept(e.getMessage(), e.getChannel()::send));
	}
	
	@Override
	public <R extends T> CompletableFuture<R> request(T request)
	{
		return helper.request(request, msg -> parent.send(msg));
	}
}
