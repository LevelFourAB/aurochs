package se.l4.aurochs.core.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Representation of a stream of bytes that can be opened.
 * 
 * @author Andreas Holstenson
 *
 */
public interface Bytes
{
	/**
	 * Open an {@link InputStream} for this instance.
	 * 
	 * @return
	 * @throws IOException
	 */
	InputStream asInputStream()
		throws IOException;
	
	/**
	 * Convert this instance to a byte array.
	 * 
	 * @return
	 * @throws IOException
	 */
	byte[] toByteArray()
		throws IOException;
	
	/**
	 * Stream this instance to the given consumer.
	 * 
	 * @param consumer
	 * @throws IOException
	 */
	default void asChunks(ByteArrayConsumer consumer)
		throws IOException
	{
		asChunks(4096, consumer);
	}
	
	/**
	 * Stream this instance to the given consumer with a specific chunk size.
	 * 
	 * @param size
	 * @param consumer
	 * @throws IOException
	 */
	default void asChunks(int size, ByteArrayConsumer consumer)
		throws IOException
	{
		try(InputStream in = asInputStream())
		{
			byte[] buf = new byte[size];
			int len;
			while((len = in.read(buf)) != -1)
			{
				consumer.consume(buf, 0, len);
			}
		}
	}
	
	/**
	 * Open this instance as a {@link ExtendedDataInput}.
	 * 
	 * @return
	 * @throws IOException
	 */
	default ExtendedDataInput asDataInput()
		throws IOException
	{
		return new ExtendedDataInputStream(asInputStream());
	}
	
	/**
	 * Get an instance that represents no data.
	 * 
	 * @return
	 */
	static Bytes empty()
	{
		return BytesOverByteArray.EMPTY;
	}

	/**
	 * Create an instance for the given array of bytes.
	 * 
	 * @param byteArray
	 * @return
	 */
	static Bytes create(byte[] byteArray)
	{
		return new BytesOverByteArray(byteArray);
	}
	
	/**
	 * Create an instance over the given input stream.
	 * 
	 * @param stream
	 * @return
	 */
	static Bytes create(IoSupplier<InputStream> stream)
	{
		return new InputStreamBytes(stream);
	}
	
	/**
	 * Create an instance that will create data on demand. This will only call the creator
	 * when the contents of the returned instance is accessed.
	 * 
	 * @param creator
	 * @return
	 */
	static Bytes lazyViaDataOutput(IoConsumer<ExtendedDataOutput> creator)
	{
		return BytesBuilder.createViaLazyDataOutput(creator);
	}
	
	/**
	 * Create an instance that will create data on demand. This will only call the creator
	 * when the contents of the returned instance is accessed.
	 * 
	 * @param creator
	 * @param expectedSize
	 * @return
	 */
	static Bytes lazyViaDataOutput(IoConsumer<ExtendedDataOutput> creator, int expectedSize)
	{
		return BytesBuilder.createViaLazyDataOutput(creator, expectedSize);
	}
	
	/**
	 * Create an instance by running the given function and storing the result in memory.
	 * 
	 * @param creator
	 * @return
	 * @throws IOException
	 */
	static Bytes viaDataOutput(IoConsumer<ExtendedDataOutput> creator)
		throws IOException
	{
		return BytesBuilder.createViaDataOutput(creator);
	}
	
	/**
	 * Create an instance by running the given function and storing the result in memory.
	 * 
	 * @param creator
	 * @param expectedSize
	 * @return
	 * @throws IOException
	 */
	static Bytes viaDataOutput(IoConsumer<ExtendedDataOutput> creator, int expectedSize)
		throws IOException
	{
		return BytesBuilder.createViaDataOutput(creator, expectedSize);
	}
	
	static BytesBuilder create()
	{
		return new BytesBuilder();
	}
}
