package se.l4.aurochs.core.channel.rpc;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import se.l4.aurochs.core.channel.Channel;
import se.l4.aurochs.core.channel.ChannelCodec;
import se.l4.aurochs.core.io.Bytes;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class RPC
{
	private static final ScheduledExecutorService TIMEOUT_SERVICE = Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder()
		.setNameFormat("RPC Timeout %s")
		.setDaemon(true)
		.build());
	
	private RPC()
	{
	}
	
	static Future<?> registerTimeout(long timeoutInMs, Runnable action)
	{
		return TIMEOUT_SERVICE.schedule(action, timeoutInMs, TimeUnit.MILLISECONDS);
	}
	
	public static <T> RPCHelper<T> createHelper(Function<T, CompletableFuture<T>> requestHandler)
	{
		return new RPCHelper<>(requestHandler, TimeUnit.SECONDS.toMillis(10));
	}
	
	public static <T> RPCChannel<T> createChannel(Channel<Bytes> channel, ChannelCodec<Bytes, T> codec)
	{
		return new DefaultRPCChannel<>(channel.transform(new RPCChannelCodec<>(codec)), in -> {
			CompletableFuture<T> future = new CompletableFuture<>();
			future.complete(null);
			return future;
		});
	}
	
	public static <T> RPCChannel<T> createChannel(Channel<Bytes> channel, ChannelCodec<Bytes, T> codec, Function<T, CompletableFuture<T>> requestHandler)
	{
		return new DefaultRPCChannel<>(channel.transform(new RPCChannelCodec<>(codec)), requestHandler);
	}
}
