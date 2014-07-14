package se.l4.aurochs.cluster.internal;

import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.apache.bval.constraints.NotEmpty;

import se.l4.aurochs.serialization.Expose;
import se.l4.aurochs.serialization.ReflectionSerializer;
import se.l4.aurochs.serialization.Use;

@Use(ReflectionSerializer.class)
public class ClusterConfig
{
	@Expose
	private boolean client;
	
	@Expose
	@Min(value=1)
	@Max(value=65535)
	private int port;
	
	@Expose
	private MulticastNetworkConfig multicast;
	
	@Expose("static")
	private StaticNetworkConfig staticNetwork;
	
	public ClusterConfig()
	{
		port = 5701;
	}
	
	public boolean isClient()
	{
		return client;
	}
	
	public int getPort()
	{
		return port;
	}
	
	public MulticastNetworkConfig getMulticast()
	{
		return multicast;
	}
	
	public StaticNetworkConfig getStaticNetwork()
	{
		return staticNetwork;
	}
	
	@Use(ReflectionSerializer.class)
	public static class MulticastNetworkConfig
	{
		@NotNull
		@NotEmpty
		@Expose
		private String group;
		
		@NotNull
		@Expose
		@Min(value=1)
		@Max(value=65535)
		private int port;
		
		public MulticastNetworkConfig()
		{
			port = 17282;
		}
		
		public String getGroup()
		{
			return group;
		}
		
		public int getPort()
		{
			return port;
		}
	}
	
	@Use(ReflectionSerializer.class)
	public static class StaticNetworkConfig
	{
		@Expose
		@NotNull
		private List<String> hosts;
		
		public List<String> getHosts()
		{
			return hosts;
		}
	}
}
