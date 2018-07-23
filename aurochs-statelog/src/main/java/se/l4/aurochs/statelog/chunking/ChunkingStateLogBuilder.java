package se.l4.aurochs.statelog.chunking;

import java.util.Iterator;

import se.l4.aurochs.statelog.LogEntry;
import se.l4.aurochs.statelog.StateLogBuilder;
import se.l4.aurochs.statelog.StreamingStateLog;
import se.l4.aurochs.statelog.StreamingStateLogBuilder;
import se.l4.commons.id.SimpleLongIdGenerator;
import se.l4.commons.io.Bytes;
import se.l4.commons.io.IOConsumer;

public class ChunkingStateLogBuilder
	implements StreamingStateLogBuilder<Bytes>
{
	private StateLogBuilder<Bytes> builder;
	private IOConsumer<LogEntry<Iterator<Bytes>>> applier;
	private boolean isVolatile;

	public ChunkingStateLogBuilder(StateLogBuilder<Bytes> parent)
	{
		builder = parent;
	}
	
	@Override
	public StateLogBuilder<Iterator<Bytes>> withApplier(IOConsumer<LogEntry<Iterator<Bytes>>> applier)
	{
		this.applier = applier;
		this.isVolatile = false;
		return this;
	}
	
	@Override
	public StateLogBuilder<Iterator<Bytes>> withVolatileApplier(IOConsumer<LogEntry<Iterator<Bytes>>> applier)
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
