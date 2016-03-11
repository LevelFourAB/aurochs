package se.l4.aurochs.serialization.format;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

/**
 * Adapter for streaming results in different output formats.
 * 
 * @author andreas
 *
 */
public interface StreamingOutput
	extends Flushable, Closeable
{
	/**
	 * Write the start of an object.
	 * 
	 * @param name
	 */
	void writeObjectStart(String name)
		throws IOException;
	
	/**
	 * Write the end of an object.
	 * 
	 * @param name
	 * @throws IOException 
	 */
	void writeObjectEnd(String name)
		throws IOException;
	
	/**
	 * Write the start of a list.
	 * 
	 * @param name
	 */
	void writeListStart(String name)
		throws IOException;
	
	/**
	 * Write the end of a list.
	 * 
	 * @param name
	 * @throws IOException 
	 */
	void writeListEnd(String name)
		throws IOException;
	
	/**
	 * Write a string.
	 * 
	 * @param name
	 * @param value
	 * @return
	 * @throws IOException
	 */
	void write(String name, String value)
		throws IOException;
	
	/**
	 * Write an integer.
	 * 
	 * @param name
	 * @param number
	 * @return
	 * @throws IOException
	 */
	void write(String name, int number)
		throws IOException;
	
	/**
	 * Write a long.
	 * 
	 * @param name
	 * @param number
	 * @return
	 * @throws IOException
	 */
	void write(String name, long number)
		throws IOException;

	/**
	 * Write a float.
	 * 
	 * @param name
	 * @param score
	 * @return
	 * @throws IOException
	 */
	void write(String name, float number)
		throws IOException;
	
	/**
	 * Write a double.
	 * 
	 * @param name
	 * @param score
	 * @return
	 * @throws IOException
	 */
	void write(String name, double number)
		throws IOException;
	
	/**
	 * Write a boolean.
	 * 
	 * @param name
	 * @param b
	 * @return
	 * @throws IOException
	 */
	void write(String name, boolean b)
		throws IOException;
	
	/**
	 * Write a byte array to the output.
	 * 
	 * @param name
	 * @param data
	 * @throws IOException
	 */
	void write(String name, byte[] data)
		throws IOException;
	
	/**
	 * Write a null value.
	 * 
	 * @param name
	 * @return
	 * @throws IOException
	 */
	void writeNull(String name)
		throws IOException;

}