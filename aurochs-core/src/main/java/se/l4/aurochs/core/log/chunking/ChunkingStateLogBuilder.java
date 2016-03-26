package se.l4.aurochs.core.log.chunking;

import java.util.Iterator;

import se.l4.aurochs.core.log.LogEntry;
import se.l4.aurochs.core.log.StateLogBuilder;
import se.l4.aurochs.core.log.StreamingStateLog;
import se.l4.aurochs.core.log.StreamingStateLogBuilder;
import se.l4.commons.id.SimpleLongIdGenerator;
import se.l4.commons.io.Bytes;
import se.l4.commons.io.IoConsumer;

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
