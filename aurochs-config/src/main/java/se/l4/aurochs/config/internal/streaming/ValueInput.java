package se.l4.aurochs.config.internal.streaming;

import java.io.IOException;

import se.l4.aurochs.serialization.format.StreamingInput;

public class ValueInput
	implements StreamingInput
{
	private final Object value;
	private boolean used;

	public ValueInput(Object value)
	{
		this.value = value;
	}

	@Override
	public Token peek()
		throws IOException
	{
		return used ? null : Token.VALUE;
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
			return Token.VALUE;
		}
	}

	@Override
	public Token next(Token expected)
		throws IOException
	{
		Token token = next();
		if(expected != Token.VALUE)
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
		return Token.VALUE;
	}

	@Override
	public Object getValue()
	{
		return value;
	}

	@Override
	public String getString()
	{
		return String.valueOf(value);
	}

	@Override
	public boolean getBoolean()
	{
		return value instanceof Boolean 
			? (Boolean) value 
			: Boolean.parseBoolean(getString());
	}

	@Override
	public double getDouble()
	{
		return ((Number) value).doubleValue();
	}
	
	@Override
	public float getFloat()
	{
		return ((Number) value).floatValue();
	}
	
	@Override
	public long getLong()
	{
		return ((Number) value).longValue();
	}
	
	@Override
	public int getInt()
	{
		return ((Number) value).intValue();
	}
	
	@Override
	public short getShort()
	{
		return ((Number) value).shortValue();
	}

	@Override
	public byte[] getByteArray()
	{
		return (byte[]) value;
	}
}
