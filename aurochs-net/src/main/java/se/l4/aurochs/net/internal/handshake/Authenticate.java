package se.l4.aurochs.net.internal.handshake;

import javax.xml.bind.DatatypeConverter;

/**
 * Authentication message.
 * 
 * @author Andreas Holstenson
 *
 */
public class Authenticate
{
	private final String method;
	private final byte[] payload;

	public Authenticate()
	{
		this(null, null);
	}
	
	public Authenticate(String method, byte[] payload)
	{
		this.method = method;
		this.payload = payload;
	}
	
	public String getMethod()
	{
		return method;
	}
	
	public byte[] getPayload()
	{
		return payload;
	}
	
	@Override
	public String toString()
	{
		if(method == null)
		{
			return "AUTH";
		}
		else
		{
			return "AUTH " + method + " " + DatatypeConverter.printBase64Binary(payload);
		}
	}
}
