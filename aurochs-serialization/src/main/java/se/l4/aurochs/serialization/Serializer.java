package se.l4.aurochs.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.Function;

import se.l4.aurochs.serialization.format.BinaryInput;
import se.l4.aurochs.serialization.format.BinaryOutput;
import se.l4.aurochs.serialization.format.StreamingInput;
import se.l4.aurochs.serialization.format.StreamingOutput;

import com.google.common.base.Throwables;

/**
 * Serializer for a specific class. A serializer is used to read and write
 * objects and is usually bound to a specific class. Serializers are retrieved
 * via a {@link SerializerCollection}.
 * 
 * @author Andreas Holstenson
 *
 * @param <T>
 */
public interface Serializer<T>
	extends SerializerOrResolver
{
	/**
	 * Read an object from the specified stream.
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	T read(StreamingInput in)
		throws IOException;
	
	/**
	 * Write and object to the specified stream.
	 * 
	 * @param object
	 * 		object to write
	 * @param name
	 * 		the name it should have in the stream
	 * @param stream
	 * 		the stream to use
	 * @throws IOException
	 */
	void write(T object, String name, StreamingOutput stream)
		throws IOException;
	
	/**
	 * Get the definition that describes what this serializer can
	 * read and write.
	 * 
	 * @return
	 */
	SerializerFormatDefinition getFormatDefinition();
	
	/**
	 * Create a new function that turns objects into byte arrays.
	 * 
	 * @return
	 */
	default Function<T, byte[]> toBytes()
	{
		return (data) -> {
			if(data == null) return null;
			
			try
			{
				ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
				BinaryOutput out = new BinaryOutput(baos);
				this.write(data, "root", out);
				out.flush();
				return baos.toByteArray();
			}
			catch(IOException e)
			{
				throw Throwables.propagate(e);
			}
		};
	}
	
	/**
	 * Create a new function that turns byte arrays into objects.
	 * 
	 * @return
	 */
	default Function<byte[], T> fromBytes()
	{
		return (data) -> {
			if(data == null) return null;
			
			try
			{
				ByteArrayInputStream in = new ByteArrayInputStream(data);
				BinaryInput bin = new BinaryInput(in);
				return this.read(bin);
			}
			catch(IOException e)
			{
				throw Throwables.propagate(e);
			}
		};
	}
}
