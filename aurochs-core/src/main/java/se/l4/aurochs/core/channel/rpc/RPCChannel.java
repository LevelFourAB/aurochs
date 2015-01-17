package se.l4.aurochs.core.channel.rpc;

import java.util.concurrent.CompletableFuture;

public interface RPCChannel<T>
{
	<R extends T> CompletableFuture<R> request(T request);
}
