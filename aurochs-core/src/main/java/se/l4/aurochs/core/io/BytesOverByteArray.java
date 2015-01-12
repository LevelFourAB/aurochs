package se.l4.aurochs.core.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class BytesOverByteArray
	implements Bytes
{
	private final byte[] data;

	public BytesOverByteArray(byte[] data)
	{
		this.data = data;
	}
	
	@Override
	public InputStream asInputStream()
		throws IOException
	{
		return new ByteArrayInputStream(data);
	}
	
	@Override
	public byte[] toByteArray()
		throws IOException
	{
		return data;
	}
	
	@Override
	public byte[] toByteArray(int offset, int length)
		throws IOException
	{
		return Arrays.copyOfRange(data, offset, offset+length);
	}
}