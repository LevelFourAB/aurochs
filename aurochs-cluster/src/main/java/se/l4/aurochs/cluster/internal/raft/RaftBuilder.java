package se.l4.aurochs.cluster.internal.raft;

import java.io.File;
import java.util.function.Consumer;

import se.l4.aurochs.cluster.ClusteredStateLogBuilder;
import se.l4.aurochs.cluster.internal.raft.log.InMemoryLog;
import se.l4.aurochs.cluster.internal.raft.log.Log;
import se.l4.aurochs.cluster.internal.raft.log.StoredLogEntry;
import se.l4.aurochs.cluster.internal.raft.messages.RaftMessage;
import se.l4.aurochs.cluster.nodes.NodeSet;
import se.l4.aurochs.core.io.Bytes;
import se.l4.aurochs.core.io.IoConsumer;
import se.l4.aurochs.core.log.DefaultLogEntry;
import se.l4.aurochs.core.log.LogEntry;
import se.l4.aurochs.core.log.StateLog;
import se.l4.aurochs.core.log.StateLogBuilder;

public class RaftBuilder
	implements ClusteredStateLogBuilder<Bytes>
{
	private String id;
	private NodeSet<RaftMessage> nodes;
	
	private StateStorage stateStorage;
	private Log log;
	
	private IoConsumer<LogEntry<Bytes>> applier;
	private boolean applierVolatile;
	private Consumer<String> leaderListener;
	private IoConsumer<StoredLogEntry> rawApplier;
	
	public RaftBuilder()
	{
	}
	
	@Override
	public RaftBuilder withNodes(NodeSet<Bytes> nodes, String selfId)
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
	public RaftBuilder withLeaderListener(Consumer<String> listener)
	{
		this.leaderListener = listener;
		
		return this;
	}
	
	@Override
	public RaftBuilder inMemory()
	{
		stateStorage = new InMemoryStateStorage();
		log = new InMemoryLog();
		return this;
	}
	
	@Override
	public RaftBuilder stateInFile(File file)
	{
		MVStoreFileStorage storage = new MVStoreFileStorage(file);
		stateStorage = storage;
		log = storage;
		return this;
	}
	
	@Override
	public StateLogBuilder<Bytes> withApplier(IoConsumer<LogEntry<Bytes>> applier)
	{
		this.applier = applier;
		this.applierVolatile = false;
		return this;
	}
	
	@Override
	public StateLogBuilder<Bytes> withVolatileApplier(IoConsumer<LogEntry<Bytes>> applier)
	{
		this.applier = applier;
		this.applierVolatile = true;
		return this;
	}
	
	public RaftBuilder withRawApplier(IoConsumer<StoredLogEntry> applier)
	{
		this.rawApplier = applier;
		return this;
	}
	
	@Override
	public StateLog<Bytes> build()
	{
		IoConsumer<StoredLogEntry> applier = createApplier();
		Raft raft = new Raft(stateStorage, log, nodes, id, applier, applierVolatile, leaderListener, 15, 180, 300);
		
		return raft;
	}

	private IoConsumer<StoredLogEntry> createApplier()
	{
		if(rawApplier != null)
		{
			return rawApplier;
		}
		
		if(applier == null) throw new IllegalStateException("Need to specify an applier of events");
		
		IoConsumer<LogEntry<Bytes>> applier = this.applier;
		return (in) -> applier.accept(new DefaultLogEntry<Bytes>(in.getIndex(), LogEntry.Type.DATA, in.getData()));
	}
}
