package se.l4.aurochs.statelog.chunking;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.carrotsearch.hppc.LongArrayList;
import com.google.common.base.Throwables;

import se.l4.aurochs.statelog.DefaultLogEntry;
import se.l4.aurochs.statelog.LogData;
import se.l4.aurochs.statelog.LogEntry;
import se.l4.aurochs.statelog.StateLog;
import se.l4.aurochs.statelog.StateLogBuilder;
import se.l4.aurochs.statelog.StateLogStoreOperation;
import se.l4.aurochs.statelog.StreamingStateLog;
import se.l4.commons.id.LongIdGenerator;
import se.l4.commons.io.Bytes;
import se.l4.commons.io.ExtendedDataInput;
import se.l4.commons.io.IOConsumer;

/**
 * Implementation of {@link StreamingStateLog}.
 * 
 * @author Andreas Holstenson
 *
 */
public class ChunkingStateLogImpl
	implements StreamingStateLog<Bytes>
{
	private final LongIdGenerator ids;
	private final StateLog<Bytes> parent;
	
	private final IOConsumer<LogEntry<Iterator<Bytes>>> consumer;

	public ChunkingStateLogImpl(
			LongIdGenerator ids, 
			StateLogBuilder<Bytes> parentBuilder,
			boolean isVolatile,
			IOConsumer<LogEntry<Iterator<Bytes>>> consumer)
	{
		this.ids = ids;
		this.consumer = consumer;
		
		parent = parentBuilder.withApplier(this::applyChunk, isVolatile).build();
	}

	/**
	 * Apply the given chunk of data. This will check what kind of data is
	 * represented and act upon it if it is a commit.
	 * 
	 * @param entry
	 * @throws IOException
	 */
	private void applyChunk(LogEntry<Bytes> entry)
		throws IOException
	{
		Bytes bytes = entry.data();
		try(ExtendedDataInput in = bytes.asDataInput())
		{
			// Read the identifier
			long id = in.readVLong();
			
			// Read the type of message
			int type = in.readUnsignedByte();
			if(type == 2 /* COMMIT */)
			{
				LogData<Bytes> data = parent.data();
				
				// Commit the data
				int chunks = in.readVInt();
				Iterator<Bytes> it = new Iterator<Bytes>()
				{
					int i = 0;
					
					@Override
					public boolean hasNext()
					{
						return i < chunks;
					}
					
					@Override
					public Bytes next()
					{
						try
						{
							long chunk = in.readVLong();
							LogEntry<Bytes> chunkEntry = data.get(chunk);
							i++;
							return chunkEntry.data();
						}
						catch(IOException e)
						{
							throw Throwables.propagate(e);
						}
					}
				};
				
				consumer.accept(new DefaultLogEntry<Iterator<Bytes>>(id, LogEntry.Type.DATA, it));
			}
		}
	}
	
	@Override
	public LogData<Iterator<Bytes>> data()
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public StateLogStoreOperation<Bytes> store()
	{
		long id = ids.next();
		return new StateStoreOperationImpl(id);
	}
	
	@Override
	public void close()
	{
		parent.close();
	}

	private class StateStoreOperationImpl
		implements StateLogStoreOperation<Bytes>
	{
		private final long id;
		private final LongArrayList chunks;

		public StateStoreOperationImpl(long id)
		{
			this.id = id;
			chunks = new LongArrayList();
		}
		
		@Override
		public void store(Bytes item)
			throws IOException
		{
			CompletableFuture<LogEntry<Bytes>> future = parent.submit(Bytes.lazyViaDataOutput((out) -> {
				out.writeVLong(id);
				out.write(0);
				out.writeBytes(item);
			}));
			
			try
			{
				LogEntry<Bytes> result = future.get(30, TimeUnit.SECONDS);
				chunks.add(result.id());
			}
			catch(InterruptedException | ExecutionException | TimeoutException e)
			{
				throw new IOException("Unable to store a chunk in the state log; " + e.getMessage(), e);
			}
		}
		
		@Override
		public void abort()
			throws IOException
		{
			CompletableFuture<LogEntry<Bytes>> future = parent.submit(Bytes.lazyViaDataOutput((out) -> {
				out.writeVLong(id);
				out.write(2);
			}, 9));
			
			try
			{
				future.get(30, TimeUnit.SECONDS);
			}
			catch(InterruptedException | ExecutionException | TimeoutException e)
			{
				throw new IOException("Unable to store a chunk in the state log; " + e.getMessage(), e);
			}
		}
		
		@Override
		public CompletableFuture<Void> commit()
		{
			return parent.submit(Bytes.lazyViaDataOutput((out) -> {
				out.writeVLong(id);
				out.write(1);
				out.writeVInt(chunks.elementsCount);
				for(int i=0, n=chunks.elementsCount; i<n; i++)
				{
					out.writeVLong(chunks.buffer[i]);
				}
			}, 9 * 8 * chunks.size())).thenApply(r -> null);
		}
	}
}
