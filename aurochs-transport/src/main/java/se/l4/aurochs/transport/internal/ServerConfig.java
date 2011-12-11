package se.l4.aurochs.transport.internal;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import se.l4.aurochs.serialization.Expose;
import se.l4.aurochs.serialization.ReflectionSerializer;
import se.l4.aurochs.serialization.Use;

/**
 * Configuration values for the server.
 * 
 * @author Andreas Holstenson
 *
 */
@Use(ReflectionSerializer.class)
public class ServerConfig
{
	@Expose
	@Min(1) @Max(65535)
	private int port;
	
	public ServerConfig()
	{
		port = 7400;
	}
	
	public int getPort()
	{
		return port;
	}
}
