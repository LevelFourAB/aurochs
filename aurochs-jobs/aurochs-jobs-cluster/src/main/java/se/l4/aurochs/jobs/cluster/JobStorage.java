package se.l4.aurochs.jobs.cluster;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import se.l4.aurochs.serialization.AllowAny;
import se.l4.aurochs.serialization.Expose;
import se.l4.aurochs.serialization.ReflectionSerializer;
import se.l4.aurochs.serialization.Serializer;
import se.l4.aurochs.serialization.Use;

import com.google.common.collect.Lists;

public class JobStorage
{
	private final Serializer<StoredJob> serializer;
	
	private final MVStore store;
	private final MVMap<Long, byte[]> jobs;
	private final MVMap<long[], Long> order;
	
	public JobStorage(Serializer<StoredJob> serializer, File data)
	{
		this.serializer = serializer;
		
		store = new MVStore.Builder()
			.fileName(data.getAbsolutePath())
			.compress()
			.open();
		
		jobs = store.openMap("data");
		order = store.openMap("order");
	}
	
	public void close()
	{
		store.close();
	}

	public List<StoredJob> getNextBatch()
	{
		long stop = System.currentTimeMillis();
		
		long[] first = order.firstKey();
		if(first == null) return Collections.emptyList();
		
		List<StoredJob> results = Lists.newArrayList();
		Iterator<long[]> it = order.keyIterator(first);
		while(it.hasNext())
		{
			long[] key = it.next();
			if(key[0] > stop)
			{
				break;
			}
			
			byte[] data = jobs.get(order.get(key));
			StoredJob obj = serializer.fromBytes(data);
			if(obj.timeout < stop)
			{
				results.add(obj);
			}
		}
		
		return results;
	}
	
	public StoredJob addJob(long id, long time, int prio, Object data)
	{
		StoredJob stored = new StoredJob(id, time, prio, data, 0, 1000*60*10, 0);
		jobs.put(id, serializer.toBytes(stored));
		order.put(new long[] { time, prio, id }, id);
		return stored;
	}
	
	public void updateJobAsRunning(long id, String node, long timeout)
	{
		StoredJob job = get(id);
		job = new StoredJob(id, job.time, job.prio, job.data, job.attempt + 1, job.maxTimeInMs, timeout);
		jobs.put(id, serializer.toBytes(job));
	}
	
	public void completeJob(long id)
	{
		StoredJob job = get(id);
		order.remove(new long[] { job.time, job.prio, id });
		jobs.remove(id);
	}
	
	public void failJob(long id, long retryAt)
	{
		StoredJob job = get(id);
		job = new StoredJob(id, retryAt, job.prio, job.data, job.attempt, job.maxTimeInMs, 0);
		jobs.put(id, serializer.toBytes(job));
	}
	
	public StoredJob get(long id)
	{
		byte[] data = jobs.get(id);
		if(data == null) return null;
		return serializer.fromBytes(data);
	}
	
	@Use(ReflectionSerializer.class)
	public static class StoredJob
	{
		@Expose
		private final long id;
		
		@Expose
		private final long time;
		
		@Expose
		private final int prio;
		
		@Expose
		@AllowAny
		private final Object data;
		
		@Expose
		private final int attempt;

		@Expose
		private final long maxTimeInMs;
		
		@Expose
		private final long timeout;
		
		public StoredJob(
				@Expose("id") long id,
				@Expose("time") long time,
				@Expose("prio") int prio,
				@Expose("data") Object data,
				@Expose("attempt") int attempt,
				@Expose("maxTimeInMs") long maxTimeInMs,
				@Expose("timeout") long timeout)
		{
			this.id = id;
			this.time = time;
			this.prio = prio;
			this.data = data;
			this.attempt = attempt;
			this.maxTimeInMs = maxTimeInMs;
			this.timeout = timeout;
		}
		
		public long getId()
		{
			return id;
		}
		
		public Object getData()
		{
			return data;
		}
		
		public int getAttempt()
		{
			return attempt;
		}
		
		public long getMaxTimeInMs()
		{
			return maxTimeInMs;
		}
	}
}
