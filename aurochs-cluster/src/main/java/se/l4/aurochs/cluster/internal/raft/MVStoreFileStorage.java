package se.l4.aurochs.cluster.internal.raft;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.StreamStore;
import org.h2.mvstore.WriteBuffer;
import org.h2.mvstore.type.DataType;
import org.h2.mvstore.type.ObjectDataType;

import se.l4.aurochs.cluster.internal.raft.log.DefaultLogEntry;
import se.l4.aurochs.cluster.internal.raft.log.Log;
import se.l4.aurochs.cluster.internal.raft.log.LogEntry;
import se.l4.aurochs.core.io.Bytes;

import com.google.common.io.ByteStreams;

public class MVStoreFileStorage
	implements StateStorage, Log
{
	private final MVStore store;
	
	private final MVMap<String, Object> state;
	private final MVMap<Long, LogEntryData> log;
	private final StreamStore data;
	
	private long term;

	public MVStoreFileStorage(File file)
	{
		store = MVStore.open(file.getAbsolutePath());
		
		state = store.openMap("state");
		loadState();
		
		log = store.openMap("log", new MVMap.Builder<Long, LogEntryData>()
			.keyType(new ObjectDataType())
			.valueType(new LogEntryDataType()
		));
		data = new StreamStore(store.openMap("log.data"));
	}
	
	private void loadState()
	{
		this.term = (Long) state.get("term");
	}
	
	@Override
	public void close()
	{
		store.close();
	}
	
	@Override
	public long getCurrentTerm()
	{
		return term;
	}
	
	@Override
	public void updateCurrentTerm(long term)
	{
		this.term = term;
		state.put("term", term);
	}
	
	@Override
	public String getVote(long term)
	{
		return (String) state.get("votes." + term);
	}
	
	@Override
	public void updateVote(long term, String id)
	{
		state.put("votes." + term, id);
	}
	
	@Override
	public void clearVotes()
	{
		Iterator<String> it = state.keyIterator("votes.1");
		while(it.hasNext())
		{
			String key = it.next();
			if(! key.startsWith("votes.")) break;
			
			it.remove();
		}
	}
	
	@Override
	public long getApplyIndex()
	{
		return (Long) state.get("applyIndex");
	}
	
	@Override
	public void updateApplyIndex(long index)
	{
		state.put("applyIndex", index);
	}
	
	@Override
	public long first()
	{
		Long index = log.firstKey();
		return index == null ? 0 : index;
	}
	
	@Override
	public long last()
	{
		Long index = log.lastKey();
		return index == null ? 0 : index;
	}
	
	@Override
	public LogEntry get(long index)
		throws IOException
	{
		LogEntryData data = log.get(index);
		if(data == null) return null;
		return new DefaultLogEntry(data.index, data.term, data.type, new BytesImpl(data.data));
	}
	
	@Override
	public long store(long term, LogEntry.Type type, Bytes data)
		throws IOException
	{
		byte[] dataId;
		try(InputStream in = data.asInputStream())
		{
			dataId = this.data.put(in);
		}
		
		long nextIndex = last() + 1;
		log.put(nextIndex, new LogEntryData(nextIndex, term, type, dataId));
		
		return nextIndex;
	}
	
	@Override
	public void resetTo(long index)
		throws IOException
	{
		Iterator<Long> it = log.keyIterator(index);
		while(it.hasNext())
		{
			it.next();
			it.remove();
		}
	}
	
	private class BytesImpl
		implements Bytes
	{
		private final byte[] id;

		public BytesImpl(byte[] id)
		{
			this.id = id;
		}
		
		@Override
		public InputStream asInputStream()
			throws IOException
		{
			return data.get(id);
		}
		
		@Override
		public byte[] toByteArray()
			throws IOException
		{
			try(InputStream in = asInputStream())
			{
				return ByteStreams.toByteArray(in);
			}
		}
	}
	
	private static class LogEntryData
	{
		private final long index;
		private final long term;
		private final LogEntry.Type type;
		private final byte[] data;
		
		public LogEntryData(long index, long term, LogEntry.Type type, byte[] data)
		{
			this.index = index;
			this.term = term;
			this.type = type;
			this.data = data;
		}
	}
	
	private static class LogEntryDataType
		implements DataType
	{
		@Override
		public int compare(Object a, Object b)
		{
			return Long.compare(((LogEntryData) a).index, ((LogEntryData) b).index);
		}
		
		@Override
		public int getMemory(Object obj)
		{
			LogEntryData le = (LogEntryData) obj;
			return 4 + 8 + 8 + 16 + le.data.length;
		}
		
		@Override
		public Object read(ByteBuffer buff)
		{
			long index = buff.getLong();
			long term = buff.getLong();
			LogEntry.Type type = LogEntry.Type.values()[buff.get()];
			int len = buff.getInt();
			byte[] data = new byte[len];
			buff.get(data);
			return new LogEntryData(index, term, type, data);
		}
		
		@Override
		public void read(ByteBuffer buff, Object[] obj, int len, boolean key)
		{
			for(int i=0; i<len; i++)
			{
				obj[i] = read(buff);
			}
		}
		
		@Override
		public void write(WriteBuffer buff, Object obj)
		{
			LogEntryData le = (LogEntryData) obj;
			buff.putLong(le.index);
			buff.putLong(le.term);
			buff.put((byte) le.type.ordinal());
			buff.putInt(le.data.length);
			buff.put(le.data);
		}
		
		@Override
		public void write(WriteBuffer buff, Object[] obj, int len, boolean key)
		{
			for(int i=0; i<len; i++)
			{
				write(buff, obj[i]);
			}
		}
	}
}
