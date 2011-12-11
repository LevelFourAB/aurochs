package se.l4.aurochs.transport.internal.handshake;

/**
 * Message that indicates that the last received message was ok.
 * 
 * @author Andreas Holstenson
 *
 */
public class Ok
{
	public Ok()
	{
	}
	
	@Override
	public String toString()
	{
		return "OK";
	}
}
