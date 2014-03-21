package se.l4.aurochs.serialization;

import java.io.IOException;

import se.l4.aurochs.serialization.format.StreamingInput;
import se.l4.aurochs.serialization.format.StreamingOutput;

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
}
