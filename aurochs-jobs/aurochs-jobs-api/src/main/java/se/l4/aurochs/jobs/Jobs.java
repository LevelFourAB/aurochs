package se.l4.aurochs.jobs;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import se.l4.aurochs.serialization.Named;
import se.l4.aurochs.serialization.SerializerCollection;

/**
 * Jobs interface, for submitting and scheduling jobs. Jobs are represented
 * by their data, that must be a {@link SerializerCollection serializable} class
 * that has been {@link Named given a name}.
 * 
 * @author Andreas Holstenson
 *
 */
public interface Jobs
{
	/**
	 * Run a job, optionally returning a result. The job will be run as soon as possible.
	 * 
	 * @param jobData
	 * @return
	 */
	<T> CompletableFuture<T> run(Object jobData);
	
	/**
	 * Queue a job. The job will be placed in the work queue and
	 * will be run at the earliest at the given time.
	 * 
	 * @param jobData
	 *   the data of the job
	 * @param whenToExecute
	 *   earliest time to run this job
	 * @throws IOException
	 */
	CompletableFuture<SubmittedJob> queue(Object jobData, When whenToRun);
	
	/**
	 * Marker for running a job as soon as possible.
	 * 
	 * @return
	 */
	static When now()
	{
		return () -> 1;
	}
	
	/**
	 * Run the job a the given timestamp in milliseconds.
	 * 
	 * @param timestamp
	 * @return
	 */
	static When at(long timestamp)
	{
		return () -> timestamp;
	}
	
	/**
	 * Run the job at the given instant.
	 * 
	 * @param instant
	 * @return
	 */
	static When at(Instant instant)
	{
		return () -> instant.toEpochMilli();
	}
	
	/**
	 * Marker for when jobs should be run. Essentially wraps a timestamp.
	 * 
	 * @author Andreas Holstenson
	 *
	 */
	interface When
	{
		/**
		 * Get the UNIX time that this instance represents. If this is
		 * -1, this represent <i>the current time</i>.
		 * 
		 * @return
		 */
		long getTimestamp();
	}
}
