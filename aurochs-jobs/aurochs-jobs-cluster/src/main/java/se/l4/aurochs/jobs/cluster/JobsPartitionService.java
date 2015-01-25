package se.l4.aurochs.jobs.cluster;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import se.l4.aurochs.cluster.StateLog;
import se.l4.aurochs.cluster.nodes.Node;
import se.l4.aurochs.cluster.nodes.NodeState;
import se.l4.aurochs.cluster.nodes.NodeStates;
import se.l4.aurochs.cluster.partitions.PartitionCreateEncounter;
import se.l4.aurochs.cluster.partitions.PartitionService;
import se.l4.aurochs.core.channel.SerializationCodec;
import se.l4.aurochs.jobs.JobException;
import se.l4.aurochs.jobs.JobRunner;
import se.l4.aurochs.jobs.cluster.JobStorage.StoredJob;
import se.l4.aurochs.jobs.cluster.messages.JobControlMessage;
import se.l4.aurochs.jobs.cluster.messages.QueueJob;
import se.l4.aurochs.jobs.cluster.op.JobDone;
import se.l4.aurochs.jobs.cluster.op.JobOperation;
import se.l4.aurochs.jobs.cluster.op.RunJob;
import se.l4.aurochs.serialization.SerializerCollection;

import com.carrotsearch.hppc.LongObjectMap;
import com.carrotsearch.hppc.LongObjectOpenHashMap;

public class JobsPartitionService
	implements PartitionService<JobControlMessage>
{
	private final JobsImpl jobs;
	
	private final int partition;
	private final StateLog<JobOperation> log;
	private final NodeStates<?> nodes;
	
	private final JobStorage storage;
	private final String localNode;
	
	private final ExecutorService jobRunningExecutor;
	private final LongObjectMap<Future<?>> jobsRunning;
	
	private final ScheduledExecutorService jobSchedulerExecutor;

	private final BlockingQueue<StoredJob> queue;
	private volatile boolean leader;

	private Thread queuer;

	private ScheduledFuture<?> queueRefiller;


	public JobsPartitionService(JobsImpl jobs,
			PartitionCreateEncounter<JobControlMessage> encounter, 
			SerializerCollection serializers,
			ExecutorService jobRunningExecutor,
			ScheduledExecutorService jobSchedulerExecutor
			)
	{
		this.partition = encounter.partition();
		this.jobs = jobs;
		
		this.jobRunningExecutor = jobRunningExecutor;
		this.jobSchedulerExecutor = jobSchedulerExecutor;
		
		storage = new JobStorage(serializers.find(StoredJob.class), new File(encounter.getDataDir(), "storage.mv"));
		
		queue = new LinkedBlockingQueue<>();
		
		jobsRunning = new LongObjectOpenHashMap<>();
		
		log = encounter.stateLog()
			.transform(SerializationCodec.newDynamicCodec(serializers, JobOperation.class))
			.withApplier(this::applyOperation)
			.build();
		
		encounter.createChannel(this::handleMessage);
		
		nodes = encounter.nodes();
		
		Node<?> node = encounter.localNode();
		this.localNode = node.getId();
		nodes.listen(e -> {
			if(e.getNode() != node) return;
			
			if(e.getState() == NodeState.LEADER)
			{
				takeControl();
			}
			else
			{
				releaseControl();
			}
		});
	}
	
	private void applyOperation(JobOperation op)
	{
		if(op instanceof QueueJob)
		{
			QueueJob j = (QueueJob) op;
			
			StoredJob stored = storage.addJob(j.getId(), j.getTime(), 1, j.getData());
			
			if(leader && j.getTime() < System.currentTimeMillis())
			{
				queue.add(stored);
			}
		}
		else if(op instanceof RunJob)
		{
			RunJob run = (RunJob) op;
			
			storage.updateJobAsRunning(run.getId(), run.getNode(), run.getTimeout());
			
			if(localNode.equals(run.getNode()))
			{
				runJob(run.getId(), run.getTimeout());
			}
			else
			{
				cancelJob(run.getId());
			}
		}
		else if(op instanceof JobDone)
		{
			JobDone result = (JobDone) op;
			switch(result.getStatus())
			{
				case COMPLETED:
					storage.completeJob(result.getId());
					// TODO: Send back result
					break;
				case FAILED:
					if(result.getRetryAt() == -1)
					{
						// Fail permanently
						storage.completeJob(result.getId());
						
						// TODO: Send back that the job failed
					}
					else
					{
						StoredJob job = storage.get(result.getId());
						if(job.getAttempt() >= 5)
						{
							// This was the last try, fail permanently
							storage.completeJob(job.getId());
							
							// TODO: Send back that the job failed
						}
						else
						{
							long retryAt = result.getRetryAt();
							if(retryAt == 0)
							{
								// TODO: Configurable back off?
								retryAt = System.currentTimeMillis() + 1000 * Math.max(1, ThreadLocalRandom.current().nextInt(1 << job.getAttempt()));
							}
							
							storage.failJob(result.getId(), retryAt);
						}
					}
					
					break;
			}
		}
	}
	
	private CompletableFuture<JobControlMessage> handleMessage(JobControlMessage message)
	{
		if(message instanceof QueueJob)
		{
			return log.submit((QueueJob) message)
				.thenApply(in -> null);
		}
		
		return CompletableFuture.completedFuture(null);
	}
	
	private void takeControl()
	{
		this.leader = true;
		queuer = new Thread(this::queueJobs, "JobQueuer[partition=" + partition + "]");
		queuer.start();
		
		queueRefiller = jobSchedulerExecutor.scheduleAtFixedRate(() -> {
			storage.getNextBatch().forEach(queue::add);
		}, 10, 100, TimeUnit.MILLISECONDS);
	}
	
	private void queueJobs()
	{
		while(! Thread.currentThread().isInterrupted() && leader)
		{
			try
			{
				StoredJob job = queue.take();
				
				Node<?> executor = nodes.random();
				
				// Submit the work order to the log
				log.submit(new RunJob(job.getId(), executor.getId(), System.currentTimeMillis() + job.getMaxTimeInMs()));
				
//				long id = job.getId();
//				jobSchedulerExecutor.schedule(() -> {
//					
//				}, job.getMaxTimeInMs() + 1000, TimeUnit.MILLISECONDS);
			}
			catch(InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}
		}
	}
	
	private void releaseControl()
	{
		this.leader = false;
		queuer.interrupt();
		queueRefiller.cancel(false);
	}
	
	/**
	 * Run the given job.
	 * 
	 * @param id
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void runJob(long id, long timeout)
	{
		Future<?> future = jobRunningExecutor.submit(() -> {
			StoredJob job = storage.get(id);
			if(job == null || timeout < System.currentTimeMillis())
			{
				// Extra protection if we have been slow with completing the job
				jobsRunning.remove(id);
				return;
			}
			
			JobImpl control = new JobImpl<>(id, job.getData(), job.getAttempt());
			
			JobRunner<?> runner = jobs.getRunner(job.getData());
			if(runner != null)
			{
				try
				{
					runner.run(control);
				}
				catch(Throwable t)
				{
					control.fail(t);
				}
				finally
				{
					control.complete();
				}
			}
			else
			{
				control.fail(new JobException("No runner registered for job of type " + job.getData().getClass()));
			}
			
			synchronized(jobsRunning)
			{
				jobsRunning.remove(id);
			}
			
			JobOperation op = control.finish(storage);
			log.submit(op);
		});
		
		synchronized(jobsRunning)
		{
			jobsRunning.put(id, future);
		}
	}
	
	/**
	 * Cancel the given job if we have it in our queue.
	 * 
	 * @param id
	 */
	private void cancelJob(long id)
	{
		Future<?> future = jobsRunning.get(id);
		if(future != null)
		{
			future.cancel(true);
		}
	}
}
