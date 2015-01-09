package se.l4.aurochs.core.io;

import java.io.IOException;
import java.io.InputStream;

public interface Bytes
{
	InputStream asInputStream()
		throws IOException;
	
	byte[] toByteArray()
		throws IOException;
	
	byte[] toByteArray(int offset, int length)
		throws IOException;
	
	default void asChunks(ByteArrayConsumer consumer)
		throws IOException
	{
		try(InputStream in = asInputStream())
		{
			byte[] buf = new byte[4096];
			int len;
			while((len = in.read(buf)) != -1)
			{
				consumer.consume(buf, 0, len);
			}
		}
	}

	static Bytes create(byte[] byteArray)
	{
		return new BytesOverByteArray(byteArray);
	}

}
