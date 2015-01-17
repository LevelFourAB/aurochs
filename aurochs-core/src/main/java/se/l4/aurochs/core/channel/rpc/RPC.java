package se.l4.aurochs.core.channel.rpc;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import se.l4.aurochs.core.channel.Channel;
import se.l4.aurochs.core.channel.ChannelCodec;
import se.l4.aurochs.core.io.Bytes;

public class RPC
{
	private RPC()
	{
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
