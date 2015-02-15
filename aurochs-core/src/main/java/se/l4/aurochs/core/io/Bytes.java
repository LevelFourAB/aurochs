package se.l4.aurochs.core.io;

import java.io.IOException;
import java.io.InputStream;

public interface Bytes
{
	InputStream asInputStream()
		throws IOException;
	
	byte[] toByteArray()
		throws IOException;
	
	default void asChunks(ByteArrayConsumer consumer)
		throws IOException
	{
		asChunks(4096, consumer);
	}
	
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
	
	default ExtendedDataInput asDataInput()
		throws IOException
	{
		return new ExtendedDataInputStream(asInputStream());
	}
	
	static Bytes empty()
	{
		return BytesOverByteArray.EMPTY;
	}

	static Bytes create(byte[] byteArray)
	{
		return new BytesOverByteArray(byteArray);
	}
	
	static Bytes lazyViaDataOutput(IoConsumer<ExtendedDataOutput> creator)
	{
		return BytesBuilder.createViaLazyDataOutput(creator);
	}
	
	static Bytes lazyViaDataOutput(IoConsumer<ExtendedDataOutput> creator, int expectedSize)
	{
		return BytesBuilder.createViaLazyDataOutput(creator, expectedSize);
	}
	
	static Bytes viaDataOutput(IoConsumer<ExtendedDataOutput> creator)
		throws IOException
	{
		return BytesBuilder.createViaDataOutput(creator);
	}
	
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
