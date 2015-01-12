package se.l4.aurochs.core.io;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ExtendedDataOutputStream
	extends DataOutputStream
	implements ExtendedDataOutput
{
	public ExtendedDataOutputStream(OutputStream out)
	{
		super(out);
	}
	
	@Override
	public void writeVInt(int value)
		throws IOException
	{
		while(true)
		{
			if((value & ~0x7F) == 0)
			{
				write(value);
				break;
			}
			else
			{
				write((value & 0x7f) | 0x80);
				value >>>= 7;
			}
		}
	}
	
	@Override
	public void writeVLong(long value)
		throws IOException
	{
		while(true)
		{
			if((value & ~0x7FL) == 0)
			{
				write((int) value);
				break;
			}
			else
			{
				write(((int) value & 0x7f) | 0x80);
				value >>>= 7;
			}
		}
	}
	
	@Override
	public void writeBytes(Bytes bytes)
		throws IOException
	{
		bytes.asChunks(8192, (data, offset, len) -> {
			writeVInt(len);
			write(data, offset, len);
		});
		writeVInt(0);
	}
}
