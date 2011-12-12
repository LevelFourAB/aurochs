package se.l4.aurochs.net.internal;

import java.io.File;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

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
	
	@Expose
	@Valid
	private TLS tls;
	
	public ServerConfig()
	{
		port = 7400;
	}
	
	public int getPort()
	{
		return port;
	}
	
	public TLS getTls()
	{
		return tls;
	}
	
	@Use(ReflectionSerializer.class)
	public static class TLS
	{
		@Expose
		@NotNull
		private File privateKey;
		
		@Expose
		@NotNull
		private File certificate;
		
		@Expose
		@NotNull
		private String password;
		
		public TLS()
		{
			password = "";
		}
		
		public File getCertificate()
		{
			return certificate;
		}
		
		public File getPrivateKey()
		{
			return privateKey;
		}
		
		public String getPassword()
		{
			return password;
		}
	}
}
