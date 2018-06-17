package se.l4.aurochs.channels.rpc;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;

public class RPCHelper<T>
{
	private final Function<T, CompletableFuture<T>> requestHandler;
	
	private final AtomicLong counter;
	private final Map<Long, CompletableFuture<T>> futures;

	private final long timeoutInMs;

	public RPCHelper(Function<T, CompletableFuture<T>> requestHandler, long timeoutInMs)
	{
		this.requestHandler = requestHandler;
		this.timeoutInMs = timeoutInMs;
		counter = new AtomicLong();
		futures = new ConcurrentHashMap<>();
	}
	
	public <R extends T> CompletableFuture<R> request(T request, Consumer<RpcRequest<T>> sender)
	{
		CompletableFuture<R> future = new CompletableFuture<R>();
		
		request(request, sender, future, 0, 1);
		
		return future;
	}
	
	@SuppressWarnings("unchecked")
	private void request(T request, Consumer<RpcRequest<T>> sender, CompletableFuture<? extends T> future, long previousId, int attempt)
	{
		if(previousId > 0)
		{
			futures.remove(previousId);
		}
		
		long id = counter.incrementAndGet();
		
		Future<?> timeout = RPC.registerTimeout(timeoutInMs, () -> {
			if(attempt > 4)
			{
				future.completeExceptionally(new TimeoutException("Request timed out"));	
			}
			else
			{
				request(request, sender, future, id, attempt + 1);
			}
		});
		
		RpcRequest<T> rpc = new DefaultRPCRequest<>(id, request);
		futures.put(id, (CompletableFuture<T>) future);
		
		future.whenComplete((a, b) -> {
			futures.remove(id);
			timeout.cancel(false); 
		});
		
		sender.accept(rpc);
	}
	
	public void accept(RpcMessage<T> message, Consumer<RpcReply<T>> sender)
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
			RpcReply<T> reply = (RpcReply<T>) message;
			CompletableFuture<T> future = futures.get(reply.getRequestId());
			if(future != null)
			{
				future.complete(reply.getPayload());
			}
		}
	}
}
