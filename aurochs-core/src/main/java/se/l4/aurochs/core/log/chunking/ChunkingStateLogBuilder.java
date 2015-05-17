package se.l4.aurochs.core.log.chunking;

import java.util.Iterator;

import se.l4.aurochs.core.id.SimpleLongIdGenerator;
import se.l4.aurochs.core.io.Bytes;
import se.l4.aurochs.core.io.IoConsumer;
import se.l4.aurochs.core.log.LogEntry;
import se.l4.aurochs.core.log.StateLogBuilder;
import se.l4.aurochs.core.log.StreamingStateLog;
import se.l4.aurochs.core.log.StreamingStateLogBuilder;

public class ChunkingStateLogBuilder
	implements StreamingStateLogBuilder<Bytes>
{
	private StateLogBuilder<Bytes> builder;
	private IoConsumer<LogEntry<Iterator<Bytes>>> applier;
	private boolean isVolatile;

	public ChunkingStateLogBuilder(StateLogBuilder<Bytes> parent)
	{
		builder = parent;
	}
	
	@Override
	public StateLogBuilder<Iterator<Bytes>> withApplier(IoConsumer<LogEntry<Iterator<Bytes>>> applier)
	{
		this.applier = applier;
		this.isVolatile = false;
		return this;
	}
	
	@Override
	public StateLogBuilder<Iterator<Bytes>> withVolatileApplier(IoConsumer<LogEntry<Iterator<Bytes>>> applier)
	{
		this.applier = applier;
		this.isVolatile = true;
		return this;
	}
	
	@Override
	public StreamingStateLog<Bytes> build()
	{
		SimpleLongIdGenerator ids = new SimpleLongIdGenerator();
		return new ChunkingStateLogImpl(ids, builder, isVolatile, applier);
	}
}
