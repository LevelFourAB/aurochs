package se.l4.aurochs.core.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A @{link OutputStream} that will send chunks of the written data to the given @{link ByteArrayConsumer}.
 * 
 * @author Andreas Holstenson
 *
 */
public class ChunkOutputStream
	extends OutputStream
{
	private final ByteArrayConsumer out;
	private final byte[] buffer;
	private int len;

	public ChunkOutputStream(int size, ByteArrayConsumer out)
	{
		this.out = out;
		buffer = new byte[size];
	}
	
	@Override
	public void write(int b)
		throws IOException
	{
		buffer[len++] = (byte) b;
		if(len == buffer.length)
		{
			out.consume(buffer, 0, len);
			len = 0;
		}
	}
	
	@Override
	public void close()
		throws IOException
	{
		if(len != 0)
		{
			out.consume(buffer, 0, len);
			len = 0;
		}
	}
}