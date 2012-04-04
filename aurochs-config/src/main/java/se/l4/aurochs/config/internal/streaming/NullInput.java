package se.l4.aurochs.config.internal.streaming;

import java.io.IOException;

import se.l4.aurochs.serialization.format.StreamingInput;

public class NullInput
	implements StreamingInput
{
	private boolean used;

	public NullInput()
	{
	}

	@Override
	public Token peek()
		throws IOException
	{
		return used ? null : Token.NULL;
	}

	@Override
	public Token next()
		throws IOException
	{
		if(used)
		{
			return null;
		}
		else
		{
			used = true;
			return Token.NULL;
		}
	}

	@Override
	public Token next(Token expected)
		throws IOException
	{
		Token token = next();
		if(expected != Token.NULL)
		{
			throw new IOException("Expected "+ expected + " but got " + token);
		}
		
		return token;
	}

	@Override
	public void skip() throws IOException
	{
	}

	@Override
	public void skipValue() throws IOException
	{
	}

	@Override
	public Token current()
	{
		return Token.NULL;
	}

	@Override
	public Object getValue()
	{
		return null;
	}

	@Override
	public String getString()
	{
		return null;
	}

	@Override
	public boolean getBoolean()
	{
		return false;
	}

	@Override
	public double getDouble()
	{
		return 0;
	}

	@Override
	public float getFloat()
	{
		return 0;
	}

	@Override
	public long getLong()
	{
		return 0;
	}

	@Override
	public int getInt()
	{
		return 0;
	}

	@Override
	public short getShort()
	{
		return 0;
	}

	@Override
	public byte[] getByteArray()
	{
		return null;
	}

	
}
