package se.l4.aurochs.cluster.internal.chunkedstate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import se.l4.aurochs.cluster.internal.raft.RaftBuilder;
import se.l4.aurochs.cluster.internal.raft.log.LogEntry;
import se.l4.aurochs.cluster.statelog.ChunkingStateLog;
import se.l4.aurochs.cluster.statelog.StateStoreOperation;
import se.l4.aurochs.core.id.LongIdGenerator;
import se.l4.aurochs.core.io.Bytes;
import se.l4.aurochs.core.io.ChunkOutputStream;
import se.l4.aurochs.core.io.ExtendedDataInput;
import se.l4.aurochs.core.io.IoConsumer;
import se.l4.aurochs.core.log.StateLog;

/**
 * Implementation of {@link ChunkingStateLog}.
 * 
 * @author Andreas Holstenson
 *
 */
public class ChunkingStateLogImpl
	implements ChunkingStateLog
{
	private final LongIdGenerator ids;
	private final StateLog<Bytes> parent;
	
	private final IoConsumer<InputStream> consumer;
	
	private ChunkLog log;

	public ChunkingStateLogImpl(LongIdGenerator ids, RaftBuilder<Bytes> parentBuilder, IoConsumer<InputStream> consumer)
	{
		this.ids = ids;
		this.consumer = consumer;
		parentBuilder.withRawApplier(this::applyChunk);
		
		parent = parentBuilder.build();
		
		
	}
	
	private void applyChunk(LogEntry entry)
		throws IOException
	{
		Chunk chunk = toChunk(entry.getData());
		if(chunk.type() == ChunkType.DATA)
		{
			log.storeChunk(chunk);
		}
		else if(chunk.type() == ChunkType.ABORT)
		{
			// Not going to take care of the given request
			log.remove(chunk.request());
		}
		else if(chunk.type() == ChunkType.COMMIT)
		{
			// TODO: We can apply data on our own thread if we like
			Iterator<Chunk> chunks = log.chunks(chunk.request());
			ChunkInputStream stream = new ChunkInputStream(chunks);
			consumer.accept(stream);
			log.remove(chunk.request());
		}
	}
	
	private Chunk toChunk(Bytes data)
		throws IOException
	{
		try(ExtendedDataInput in = data.asDataInput())
		{
			long id = in.readVLong();
			int type = in.readUnsignedByte();
			
			switch(type)
			{
				case 0:
					// DATA
					int len = in.readVInt();
					byte[] bytes = new byte[len];
					in.readFully(bytes);
					return new DefaultChunk(id, ChunkType.DATA, bytes);
				case 1:
					// COMMIT
					return new DefaultChunk(id, ChunkType.COMMIT, null);
				case 2:
					// ABORT
					return new DefaultChunk(id, ChunkType.ABORT, null);
			}
			
			throw new IOException("Type of chunk not recognized: " + type);
		}
	}

	@Override
	public StateStoreOperation store()
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
		implements StateStoreOperation
	{
		private final long id;
		private final ChunkOutputStream out;

		public StateStoreOperationImpl(long id)
		{
			this.id = id;
			
			out = new ChunkOutputStream(8192, this::sendChunk);
		}
		
		private void sendChunk(byte[] data, int offset, int length)
			throws IOException
		{
			CompletableFuture<Void> future = parent.submit(Bytes.viaDataOutput((out) -> {
				out.writeVLong(id);
				out.write(0);
				out.writeVInt(length);
				out.write(data, offset, length);
			}, length + 13));
			
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
		public OutputStream stream()
		{
			return out;
		}
		
		@Override
		public void abort()
			throws IOException
		{
			CompletableFuture<Void> future = parent.submit(Bytes.lazyViaDataOutput((out) -> {
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
			}, 9));
		}
	}
	
	private static class ChunkInputStream
		extends InputStream
	{
		private Iterator<Chunk> it;
		private InputStream in;

		public ChunkInputStream(Iterator<Chunk> it)
			throws IOException
		{
			this.it = it;
			
			next();
		}

		@Override
		public void close()
			throws IOException
		{
			if(in != null)
			{
				try
				{
					in.close();
				}
				finally
				{
					in = null;
				}
			}
		}

		private void next()
			throws IOException
		{
			close();
			
			if(it.hasNext())
			{
				in = new ByteArrayInputStream(it.next().data());
			}
		}

		@Override
		public int available()
			throws IOException
		{
			if(in == null)
			{
				return 0;
			}
			
			return in.available();
		}


		@Override
		public int read()
			throws IOException
		{
			if(in == null) return -1;
			
			int b = in.read();
			if(b == -1)
			{
				next();
				b = read();
			}
			
			return b;
		}

		@Override
		public int read(byte[] out, int off, int len)
			throws IOException
		{
			if(in == null) return -1;
			
			int b = in.read(out, off, len);
			if(b == -1)
			{
				next();
				return read(out, off, len);
			}
			return b;
		}

		@Override
		public long skip(long n)
			throws IOException
		{
			if(in == null || n <= 0) return 0;
			
			long result = in.skip(n);
			if(result != 0) return result;
			if(read() == -1) return 0;
			return 1 + in.skip(n - 1);
		}
	}
}
