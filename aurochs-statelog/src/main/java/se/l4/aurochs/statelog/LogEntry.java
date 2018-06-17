package se.l4.aurochs.statelog;

import java.io.IOException;

public interface LogEntry<T>
{
	enum Type
	{
		/**
		 * Internal log entry. Data of internal entries can't be read.
		 */
		INTERNAL,
		
		/**
		 * 
		 */
		DATA
	}
	
	/**
	 * Get the id of this entry.
	 * 
	 * @return
	 */
	long id();
	
	/**
	 * Get the type of this entry.
	 * 
	 * @return
	 */
	Type type();
	
	/**
	 * Get the data of this entry.
	 * 
	 * @return
	 */
	T data()
		throws IOException;
}
