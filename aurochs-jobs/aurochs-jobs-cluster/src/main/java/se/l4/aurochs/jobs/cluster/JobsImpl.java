package se.l4.aurochs.jobs.cluster;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.IntFunction;

import se.l4.aurochs.cluster.Cluster;
import se.l4.aurochs.cluster.partitions.PartitionChannel;
import se.l4.aurochs.core.id.LongIdGenerator;
import se.l4.aurochs.jobs.AbstractLocalJobs;
import se.l4.aurochs.jobs.JobBuilder;
import se.l4.aurochs.jobs.JobException;
import se.l4.aurochs.jobs.JobRunner;
import se.l4.aurochs.jobs.Jobs;
import se.l4.aurochs.jobs.SubmittedJob;
import se.l4.aurochs.jobs.cluster.messages.JobControlMessage;
import se.l4.aurochs.jobs.cluster.messages.QueueJob;
import se.l4.aurochs.jobs.cluster.messages.SubmittedJobImpl;
import se.l4.aurochs.jobs.cluster.messages.WaitForResult;
import se.l4.aurochs.jobs.cluster.messages.WaitOver;
import se.l4.aurochs.serialization.SerializerCollection;

import com.google.inject.Singleton;

@Singleton
public class JobsImpl
	extends AbstractLocalJobs
{
	private final LongIdGenerator ids;
	private final PartitionChannel<JobControlMessage> channel;

	public JobsImpl(Cluster cluster,
			LongIdGenerator ids,
			SerializerCollection serializers)
	{
		this.ids = ids;
		
		ScheduledExecutorService jobSchedulerExecutor = Executors.newScheduledThreadPool(1);
		ExecutorService jobRunningExecutor = Executors.newFixedThreadPool(8);
		
		this.channel = cluster.newService("aurochs:jobs")
			.partitioned()
			.withSerializingCodec(JobControlMessage.class)
			.create(encounter -> new JobsPartitionService(this, encounter, serializers, jobRunningExecutor, jobSchedulerExecutor));
	}

	@Override
	public JobBuilder add(Object jobData)
	{
		return new JobBuilderImpl(jobData);
	}
	
	@Override
	public JobRunner<?> getRunner(Object data)
	{
		return super.getRunner(data);
	}
	
	private class JobBuilderImpl
		implements JobBuilder
	{
		private final Object jobData;
		private When when;
		private boolean needsResult;

		public JobBuilderImpl(Object jobData)
		{
			this.jobData = jobData;
			when = Jobs.now();
		}
		
		@Override
		public JobBuilder delay(When when)
		{
			this.when = when;
			
			return this;
		}
		
		@Override
		public JobBuilder withResult()
		{
			this.needsResult = true;
			
			return this;
		}
		
		public <T> CompletableFuture<SubmittedJob<T>> submitAsync()
		{
			QueueJob msg = new QueueJob(ids.next(), jobData, when.getTimestamp());
			IntFunction<CompletableFuture<SubmittedJob<T>>> func = (p) -> {
				CompletableFuture<T> result;
				if(needsResult)
				{
					result = channel.sendToOneOf(p, new WaitForResult(msg.getId()))
						.thenApply(reply -> (T) ((WaitOver) reply).getData());
				}
				else
				{
					result = null;
				}
				
				return channel.sendToOneOf(p, msg)
					.thenApply(reply -> new SubmittedJobImpl<T>(result));
			};
			
			// TODO: Support sending items to a specific partition
			return channel.withRandomPartition(func);
		}
		
		@Override
		public <T> SubmittedJob<T> submit()
		{
			try
			{
				CompletableFuture<SubmittedJob<T>> future = submitAsync();
				return future.get(10, TimeUnit.SECONDS);
			}
			catch(InterruptedException e)
			{
				Thread.currentThread().interrupt();
				throw new JobException("Interrupted while waiting for results; " + e.getMessage(), e);
			}
			catch(ExecutionException e)
			{
				throw new JobException("Unable to submit job; " + e.getCause().getMessage(), e.getCause());
			}
			catch(TimeoutException e)
			{
				throw new JobException("Timed out while waiting for job to be submitted; " + e.getMessage(), e);
			}
		}
	}
}
