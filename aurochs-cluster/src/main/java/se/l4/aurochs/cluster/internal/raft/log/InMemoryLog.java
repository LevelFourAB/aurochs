package se.l4.aurochs.cluster.internal.raft.log;

import java.io.IOException;
import java.util.List;

import se.l4.aurochs.core.io.Bytes;

import com.google.common.collect.Lists;

public class InMemoryLog
	implements Log
{
	private final List<LogEntry> entries;
	
	public InMemoryLog()
	{
		entries = Lists.newArrayList();
	}
	
	@Override
	public void close()
		throws IOException
	{
		entries.clear();
	}
	
	@Override
	public long first()
	{
		return entries.isEmpty() ? 0 : 1;
	}
	
	@Override
	public long last()
	{
		return entries.size();
	}
	
	@Override
	public LogEntry get(long index)
		throws IOException
	{
		if(index > entries.size() || index < 1)
		{
			throw new IOException("The given entry " + index + " is not in this log");
		}
		
		return entries.get((int) index - 1);
	}
	
	@Override
	public void resetTo(long index)
		throws IOException
	{
		if(entries.size() > index)
		{
			entries.subList((int) index, entries.size()).clear();
		}
	}
	
	@Override
	public long store(long term, Bytes data)
		throws IOException
	{
		entries.add(new DefaultLogEntry(entries.size() + 1, term, Bytes.create(data.toByteArray())));
		return entries.size();
	}
}
