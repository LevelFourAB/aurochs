package se.l4.aurochs.core.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BytesBuilder
{
	private final ByteArrayOutputStream out;
	
	public BytesBuilder()
	{
		out = new ByteArrayOutputStream(8192);
	}
	
	public BytesBuilder addChunk(byte[] buffer)
	{
		return addChunk(buffer, 0, buffer.length);
	}
	
	public BytesBuilder addChunk(byte[] buffer, int off, int len)
	{
		out.write(buffer, off, len);
		return this;
	}
	
	public Bytes build()
	{
		return Bytes.create(out.toByteArray());
	}
	
	static Bytes createViaLazyDataOutput(IoConsumer<ExtendedDataOutput> creator)
	{
		return createViaLazyDataOutput(creator, 8192);
	}
	
	static Bytes createViaLazyDataOutput(IoConsumer<ExtendedDataOutput> creator, int expectedSize)
	{
		return new DataOutputBytes(creator, expectedSize);
	}
	
	static Bytes createViaDataOutput(IoConsumer<ExtendedDataOutput> creator)
			throws IOException
	{
		return createViaDataOutput(creator, 8192);
	}
	
	static Bytes createViaDataOutput(IoConsumer<ExtendedDataOutput> creator, int expectedSize)
		throws IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream(expectedSize);
		try(ExtendedDataOutput dataOut = new ExtendedDataOutputStream(out))
		{
			creator.accept(dataOut);
		}
		return Bytes.create(out.toByteArray());
	}
	
	private static class DataOutputBytes
		implements Bytes
	{
		private final IoConsumer<ExtendedDataOutput> creator;
		private final int expectedSize;

		public DataOutputBytes(IoConsumer<ExtendedDataOutput> creator, int expectedSize)
		{
			this.creator = creator;
			this.expectedSize = expectedSize;
		}
		
		@Override
		public InputStream asInputStream()
			throws IOException
		{
			return new ByteArrayInputStream(toByteArray());
		}
		
		@Override
		public byte[] toByteArray()
			throws IOException
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream(expectedSize);
			try(ExtendedDataOutput dataOut = new ExtendedDataOutputStream(out))
			{
				creator.accept(dataOut);
			}
			return out.toByteArray();
		}
		
		@Override
		public void asChunks(ByteArrayConsumer consumer)
			throws IOException
		{
			try(ExtendedDataOutput dataOut = new ExtendedDataOutputStream(new ChunkOutputStream(4096, consumer)))
			{
				creator.accept(dataOut);
			}
		}
	}
}
