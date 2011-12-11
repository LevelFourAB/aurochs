package se.l4.aurochs.transport.internal;

import org.jboss.netty.buffer.ChannelBuffer;

import se.l4.aurochs.serialization.format.StreamingInput;
import se.l4.aurochs.serialization.format.StreamingOutput;

/**
 * Factory that can create {@link StreamingInput} and {@link StreamingOutput}
 * instances.
 * 
 * @author Andreas Holstenson
 *
 */
public interface StreamFactory
{
	/**
	 * Create an input suitable for reading from the specified buffer.
	 * 
	 * @param buffer
	 * @return
	 */
	StreamingInput createInput(ChannelBuffer buffer);
	
	/**
	 * Create an input suitable for writing to the specified buffer.
	 * 
	 * @param buffer
	 * @return
	 */
	StreamingOutput createOutput(ChannelBuffer buffer);
}
