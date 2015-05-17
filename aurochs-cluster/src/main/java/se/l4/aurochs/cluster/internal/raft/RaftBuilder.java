package se.l4.aurochs.cluster.internal.raft;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import se.l4.aurochs.cluster.ClusteredStateLogBuilder;
import se.l4.aurochs.cluster.internal.raft.log.InMemoryLog;
import se.l4.aurochs.cluster.internal.raft.log.Log;
import se.l4.aurochs.cluster.internal.raft.log.LogEntry;
import se.l4.aurochs.cluster.internal.raft.messages.RaftMessage;
import se.l4.aurochs.cluster.nodes.NodeSet;
import se.l4.aurochs.core.channel.ChannelCodec;
import se.l4.aurochs.core.io.Bytes;
import se.l4.aurochs.core.io.IoConsumer;
import se.l4.aurochs.core.log.LogData;
import se.l4.aurochs.core.log.StateLog;
import se.l4.aurochs.core.log.StateLogBuilder;

public class RaftBuilder<T>
	implements ClusteredStateLogBuilder<T>
{
	private String id;
	private NodeSet<RaftMessage> nodes;
	
	private StateStorage stateStorage;
	private Log log;
	
	private ChannelCodec<Bytes, T> codec;
	private IoConsumer<T> applier;
	private boolean applierVolatile;
	private Consumer<String> leaderListener;
	private IoConsumer<LogEntry> rawApplier;
	
	public RaftBuilder()
	{
	}
	
	@Override
	public RaftBuilder<T> withNodes(NodeSet<Bytes> nodes, String selfId)
	{
		this.nodes = nodes.transform(new RaftChannelCodec());
		this.id = selfId;
		
		return this;
	}
	
	/**
	 * Add a new listener for who controls the state log.
	 * 
	 * @param leader
	 * @return
	 */
	public RaftBuilder<T> withLeaderListener(Consumer<String> listener)
	{
		this.leaderListener = listener;
		
		return this;
	}
	
	@Override
	public RaftBuilder<T> inMemory()
	{
		stateStorage = new InMemoryStateStorage();
		log = new InMemoryLog();
		return this;
	}
	
	@Override
	public RaftBuilder<T> stateInFile(File file)
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
	
	public StateLogBuilder<T> withRawApplier(IoConsumer<LogEntry> applier)
	{
		this.rawApplier = applier;
		return this;
	}
	
	@Override
	public StateLog<T> build()
	{
		IoConsumer<LogEntry> applier = createApplier();
		Raft raft = new Raft(stateStorage, log, nodes, id, applier, applierVolatile, leaderListener, 15, 180, 300);
		
		if(codec == null) return (StateLog) raft;
		
		return new StateLogImpl<>(codec, raft);
	}

	@SuppressWarnings("unchecked")
	private IoConsumer<LogEntry> createApplier()
	{
		if(rawApplier != null)
		{
			return rawApplier;
		}
		
		if(applier == null) throw new IllegalStateException("Need to specify an applier of events");
		
		IoConsumer<Bytes> applier = codec == null
			? (IoConsumer<Bytes>) this.applier
			: new IoConsumerImpl<>(codec, this.applier);
		return (in) -> applier.accept(in.getData());
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
		public LogData<T> data()
		{
			throw new UnsupportedOperationException();
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
