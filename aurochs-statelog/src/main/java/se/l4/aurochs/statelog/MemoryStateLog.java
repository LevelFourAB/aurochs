package se.l4.aurochs.statelog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import com.google.common.collect.Lists;

import se.l4.commons.io.IoConsumer;

/**
 * A {@link StateLog} that stores its data in memory. This is useful for
 * testing, small or temporary state logs.
 * 
 * @author Andreas Holstenson
 *
 * @param <T>
 */
public class MemoryStateLog<T>
	implements StateLog<T>
{
	private final IoConsumer<LogEntry<T>> applier;
	private final MemoryLogData<T> logData;

	public MemoryStateLog(IoConsumer<LogEntry<T>> applier)
	{
		this.applier = applier;
		logData = new MemoryLogData<>();
	}
	
	@Override
	public LogData<T> data()
	{
		return logData;
	}

	@Override
	public synchronized CompletableFuture<LogEntry<T>> submit(T entry)
	{
		CompletableFuture<LogEntry<T>> future = new CompletableFuture<>();
		try
		{
			LogEntry<T> logEntry = logData.add(entry);
			applier.accept(logEntry);
			future.complete(logEntry);
		}
		catch(IOException e)
		{
			future.completeExceptionally(e);
		}
		
		return future;
	}

	@Override
	public void close()
	{
		
	}
	
	private static class MemoryLogData<T>
		implements LogData<T>
	{
		private final ArrayList<T> data;
		
		public MemoryLogData()
		{
			data = Lists.newArrayListWithExpectedSize(100);
		}
		
		@Override
		public long first()
		{
			return data.isEmpty() ? 0 : 1;
		}
		
		@Override
		public long last()
		{
			return data.size();
		}
		
		@Override
		public LogEntry<T> get(long index)
		{
			T data = this.data.get((int) index);
			return new DefaultLogEntry<>(index, LogEntry.Type.DATA, data);
		}
		
		public LogEntry<T> add(T data)
		{
			int index = this.data.size() + 1;
			this.data.add(data);
			return new DefaultLogEntry<>(index, LogEntry.Type.DATA, data);
		}
	}

	public static <T> StateLogBuilder<T> create()
	{
		return new StateLogBuilder<T>()
		{
			private IoConsumer<LogEntry<T>> applier;

			@Override
			public StateLogBuilder<T> withApplier(IoConsumer<LogEntry<T>> applier)
			{
				this.applier = applier;
				return this;
			}
			
			@Override
			public StateLogBuilder<T> withVolatileApplier(IoConsumer<LogEntry<T>> applier)
			{
				return withApplier(applier);
			}
			
			@Override
			public StateLog<T> build()
			{
				return new MemoryStateLog<>(applier);
			}
		};
	}
}
