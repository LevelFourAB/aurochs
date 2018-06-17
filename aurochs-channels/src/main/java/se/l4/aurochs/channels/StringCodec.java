package se.l4.aurochs.channels;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.google.common.base.Throwables;

import se.l4.commons.io.Bytes;

public class StringCodec
	implements ChannelCodec<Bytes, String>
{
	private static final StringCodec INSTANCE = new StringCodec();
	
	private StringCodec()
	{
	}
	
	public static StringCodec get()
	{
		return INSTANCE;
	}
	
	@Override
	public boolean accepts(Bytes in)
	{
		return true;
	}
	
	@Override
	public String fromSource(Bytes object)
	{
		try
		{
			return new String(object.toByteArray(), StandardCharsets.UTF_8);
		}
		catch(IOException e)
		{
			throw Throwables.propagate(e);
		}
	}
	
	@Override
	public Bytes toSource(String object)
	{
		return Bytes.create(object.getBytes(StandardCharsets.UTF_8));
	}
}
