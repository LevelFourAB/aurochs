package se.l4.aurochs.core.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BytesBuilder
{
	private final ByteArrayOutputStream out;
	
	public BytesBuilder()
	{
		out = new ByteArrayOutputStream(8192);
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
	
	static Bytes createViaDataOutput(IoConsumer<ExtendedDataOutput> creator)
	{
		return new DataOutputBytes(creator);
	}
	
	private static class DataOutputBytes
		implements Bytes
	{
		private final IoConsumer<ExtendedDataOutput> creator;

		public DataOutputBytes(IoConsumer<ExtendedDataOutput> creator)
		{
			this.creator = creator;
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
			ByteArrayOutputStream out = new ByteArrayOutputStream();
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
	
	private static class ChunkOutputStream
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
				onChunk(buffer, len);
				len = 0;
			}
		}
		
		@Override
		public void close()
			throws IOException
		{
			if(len != 0)
			{
				onChunk(buffer, len);
				len = 0;
			}
		}
		
		private void onChunk(byte[] data, int len)
			throws IOException
		{
			out.consume(data, 0, len);
		}
	}
}
