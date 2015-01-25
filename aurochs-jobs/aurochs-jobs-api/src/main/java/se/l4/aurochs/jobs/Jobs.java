package se.l4.aurochs.jobs;

import java.time.Instant;

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
	 * Add a job that should be run.
	 * 
	 * @param jobData
	 * @return
	 */
	JobBuilder add(Object jobData);
	
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
