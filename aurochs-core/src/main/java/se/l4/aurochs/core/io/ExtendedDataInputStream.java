package se.l4.aurochs.core.io;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class ExtendedDataInputStream
	extends DataInputStream
	implements ExtendedDataInput
{
	public ExtendedDataInputStream(InputStream in)
	{
		super(in);
	}
	
	@Override
	public int readVInt()
		throws IOException
	{
		int shift = 0;
		int result = 0;
		while(shift < 32)
		{
			final byte b = (byte) read();
			result |= (b & 0x7F) << shift;
			if((b & 0x80) == 0) return result;
			
			shift += 7;
		}
		
		throw new EOFException("Invalid integer");
	}
	
	@Override
	public long readVLong()
		throws IOException
	{
		int shift = 0;
		long result = 0;
		while(shift < 64)
		{
			final byte b = (byte) read();
			result |= (long) (b & 0x7F) << shift;
			if((b & 0x80) == 0) return result;
			
			shift += 7;
		}
		
		throw new EOFException("Invalid long");
	}
	
	@Override
	public Bytes readBytes()
		throws IOException
	{
		BytesBuilder builder = Bytes.create();
		byte[] buffer = new byte[8192];
		while(true)
		{
			int len = readVInt();
			if(len == 0) return builder.build();
			
			readFully(buffer, 0, len);
			builder.addChunk(buffer, 0, len);
		}
	}
}
