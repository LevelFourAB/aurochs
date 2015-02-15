package se.l4.aurochs.cluster.internal.chunkedstate;

import java.io.File;
import java.io.InputStream;

import se.l4.aurochs.cluster.internal.raft.RaftBuilder;
import se.l4.aurochs.cluster.nodes.NodeSet;
import se.l4.aurochs.cluster.statelog.ChunkingStateLog;
import se.l4.aurochs.cluster.statelog.ChunkingStateLogBuilder;
import se.l4.aurochs.core.id.SimpleLongIdGenerator;
import se.l4.aurochs.core.io.Bytes;
import se.l4.aurochs.core.io.IoConsumer;

public class ChunkingStateLogBuilderImpl
	implements ChunkingStateLogBuilder
{
	private RaftBuilder<Bytes> builder;
	private IoConsumer<InputStream> applier;

	public ChunkingStateLogBuilderImpl()
	{
		builder = new RaftBuilder<>();
	}
	
	@Override
	public ChunkingStateLogBuilder stateInFile(File file)
	{
		builder.stateInFile(file);
		return this;
	}
	
	@Override
	public ChunkingStateLogBuilder withNodes(NodeSet<Bytes> nodes, String selfId)
	{
		builder.withNodes(nodes, selfId);
		return this;
	}
	
	@Override
	public ChunkingStateLogBuilder withApplier(IoConsumer<InputStream> applier)
	{
		this.applier = applier;
		return this;
	}
	
	@Override
	public ChunkingStateLog build()
	{
		SimpleLongIdGenerator ids = new SimpleLongIdGenerator();
		return new ChunkingStateLogImpl(ids, builder, applier);
	}
}
