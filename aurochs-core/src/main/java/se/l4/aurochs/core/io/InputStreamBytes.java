package se.l4.aurochs.core.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.google.common.io.ByteStreams;

/**
 * Implementation of {@link Bytes} over a {@link InputStream}.
 * 
 * @author Andreas Holstenson
 *
 */
public class InputStreamBytes
	implements Bytes
{
	private final IoSupplier<InputStream> in;

	public InputStreamBytes(IoSupplier<InputStream> in)
	{
		this.in = in;
	}
	
	@Override
	public InputStream asInputStream()
		throws IOException
	{
		return in.get();
	}
	
	@Override
	public byte[] toByteArray()
		throws IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try(InputStream in = this.in.get())
		{
			ByteStreams.copy(in, out);
		}
		return out.toByteArray();
	}
}
