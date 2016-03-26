package se.l4.aurochs.net.internal;

import java.io.File;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import se.l4.aurochs.net.ServerConnection;
import se.l4.commons.serialization.Expose;
import se.l4.commons.serialization.ReflectionSerializer;
import se.l4.commons.serialization.Use;

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
	@Min(1)
	private int minThreads;
	
	@Expose
	@Min(1)
	private int maxThreads;
	
	@Expose
	@Min(1)
	private int queueSize;
	
	@Expose
	@Valid
	private TLS tls;
	
	public ServerConfig()
	{
		port = ServerConnection.DEFAULT_PORT;
		
		minThreads = 16;
		maxThreads = 256;
		
		queueSize = Integer.MAX_VALUE;
	}
	
	public int getPort()
	{
		return port;
	}
	
	public ServerConfig withPort(int port)
	{
		this.port = port;
		return this;
	}
	
	public int getMinThreads()
	{
		return minThreads;
	}
	
	public ServerConfig withMinThreads(int minThreads)
	{
		this.minThreads = minThreads;
		return this;
	}
	
	public int getMaxThreads()
	{
		return maxThreads;
	}
	
	public ServerConfig withMaxThreads(int maxThreads)
	{
		this.maxThreads = maxThreads;
		return this;
	}
	
	public int getQueueSize()
	{
		return queueSize;
	}
	
	public ServerConfig withQueueSize(int queueSize)
	{
		this.queueSize = queueSize;
		return this;
	}
	
	public TLS getTls()
	{
		return tls;
	}
	
	public ServerConfig withTLS(TLS tls)
	{
		this.tls = tls;
		return this;
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
		
		public TLS withCertificate(File file)
		{
			this.certificate = file;
			return this;
		}
		
		public File getPrivateKey()
		{
			return privateKey;
		}
		
		public TLS withPrivateKey(File file)
		{
			this.privateKey = file;
			return this;
		}
		
		public String getPassword()
		{
			return password;
		}
		
		public TLS withPassword(String password)
		{
			this.password = password;
			return this;
		}
	}
}
