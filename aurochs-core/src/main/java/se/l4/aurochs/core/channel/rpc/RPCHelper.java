package se.l4.aurochs.core.channel.rpc;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;

public class RPCHelper<T>
{
	private final Function<T, CompletableFuture<T>> requestHandler;
	
	private final AtomicLong counter;
	private final Map<Long, CompletableFuture<T>> futures;

	public RPCHelper(Function<T, CompletableFuture<T>> requestHandler)
	{
		this.requestHandler = requestHandler;
		counter = new AtomicLong();
		futures = new ConcurrentHashMap<>();
	}
	
	public <R extends T> CompletableFuture<R> request(T request, Consumer<RpcRequest<T>> sender)
	{
		long id = counter.incrementAndGet();
		CompletableFuture<R> future = new CompletableFuture<R>()
			.whenComplete((a, b) -> futures.remove(this));
		
		RpcRequest<T> rpc = new DefaultRPCRequest<>(id, request);
		futures.put(id, (CompletableFuture) future);
		
		sender.accept(rpc);
		
		return future;
	}
	
	public void accept(RpcMessage message, Consumer<RpcReply<T>> sender)
	{
		if(message instanceof RpcRequest)
		{
			RpcRequest<T> req = (RpcRequest<T>) message;
			requestHandler.apply(req.getPayload())
				.whenComplete((result, ex) -> {
					// TODO: How do we signal exceptions?
					sender.accept(new DefaultRPCReply<>(req.getRequestId(), result));
				});
		}
		else if(message instanceof RpcReply)
		{
			RpcReply<T> reply = (RpcReply) message;
			CompletableFuture<T> future = futures.get(reply.getRequestId());
			if(future != null)
			{
				future.complete(reply.getPayload());
			}
		}
	}
}
