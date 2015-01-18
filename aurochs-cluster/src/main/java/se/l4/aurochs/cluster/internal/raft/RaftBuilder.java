package se.l4.aurochs.cluster.internal.raft;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import se.l4.aurochs.cluster.StateLog;
import se.l4.aurochs.cluster.StateLogBuilder;
import se.l4.aurochs.cluster.internal.raft.log.InMemoryLog;
import se.l4.aurochs.cluster.internal.raft.log.Log;
import se.l4.aurochs.cluster.internal.raft.messages.RaftMessage;
import se.l4.aurochs.cluster.nodes.Nodes;
import se.l4.aurochs.core.channel.ChannelCodec;
import se.l4.aurochs.core.io.Bytes;
import se.l4.aurochs.core.io.IoConsumer;

public class RaftBuilder<T>
	implements StateLogBuilder<T>
{
	private String id;
	private Nodes<RaftMessage> nodes;
	
	private StateStorage stateStorage;
	private Log log;
	
	private ChannelCodec<Bytes, T> codec;
	private IoConsumer<T> applier;
	private boolean applierVolatile;
	
	public RaftBuilder()
	{
	}
	
	@Override
	public StateLogBuilder<T> withNodes(Nodes<Bytes> nodes, String selfId)
	{
		this.nodes = nodes.transform(new RaftChannelCodec());
		this.id = selfId;
		
		return this;
	}
	
	@Override
	public <O> StateLogBuilder<O> transform(ChannelCodec<Bytes, O> codec)
	{
		this.codec = (ChannelCodec) codec;
		return (StateLogBuilder) this;
	}
	
	@Override
	public StateLogBuilder<T> inMemory()
	{
		stateStorage = new InMemoryStateStorage();
		log = new InMemoryLog();
		return this;
	}
	
	@Override
	public StateLogBuilder<T> stateInFile(File file)
	{
		MVStoreFileStorage storage = new MVStoreFileStorage(file);
		stateStorage = storage;
		log = storage;
		return this;
	}
	
	@Override
	public StateLogBuilder<T> withApplier(IoConsumer<T> applier)
	{
		this.applier = applier;
		this.applierVolatile = false;
		return this;
	}
	
	@Override
	public StateLogBuilder<T> withVolatileApplier(IoConsumer<T> applier)
	{
		this.applier = applier;
		this.applierVolatile = true;
		return this;
	}
	
	@Override
	public StateLog<T> build()
	{
		IoConsumer<Bytes> applier = createApplier();
		Raft raft = new Raft(stateStorage, log, nodes, id, applier, applierVolatile, 75, 150, 300);
		
		if(codec == null) return (StateLog) raft;
		
		return new StateLogImpl<>(codec, raft);
	}

	private IoConsumer<Bytes> createApplier()
	{
		if(applier == null) throw new IllegalStateException("Need to specify an applier of events");
		
		if(codec == null) return (IoConsumer) applier;
		
		return new IoConsumerImpl<>(codec, applier);
	}
	
	private static class IoConsumerImpl<T>
		implements IoConsumer<Bytes>
	{
		private final ChannelCodec<Bytes, T> codec;
		private final IoConsumer<T> consumer;
		
		public IoConsumerImpl(ChannelCodec<Bytes, T> codec, IoConsumer<T> consumer)
		{
			this.codec = codec;
			this.consumer = consumer;
		}
		
		@Override
		public void accept(Bytes item)
			throws IOException
		{
			consumer.accept(codec.fromSource(item));
		}
	}
	
	private static class StateLogImpl<T>
		implements StateLog<T>
	{
		private final ChannelCodec<Bytes, T> codec;
		private final StateLog<Bytes> log;

		public StateLogImpl(ChannelCodec<Bytes, T> codec, StateLog<Bytes> log)
		{
			this.codec = codec;
			this.log = log;
		}
		
		@Override
		public CompletableFuture<Void> submit(T entry)
		{
			return log.submit(codec.toSource(entry));
		}
		
		@Override
		public void close()
		{
			log.close();
		}
	}
}
