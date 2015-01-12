package se.l4.aurochs.cluster.internal;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import se.l4.aurochs.cluster.Cluster;
import se.l4.aurochs.net.internal.ServerConfig;
import se.l4.aurochs.serialization.Expose;
import se.l4.aurochs.serialization.ReflectionSerializer;
import se.l4.aurochs.serialization.Use;
import se.l4.aurochs.serialization.enums.IgnoreCaseNameTranslator;
import se.l4.aurochs.serialization.enums.MapEnumVia;

@Use(ReflectionSerializer.class)
public class ClusterConfig
{
	@Expose
	@NotNull
	@MapEnumVia(IgnoreCaseNameTranslator.class)
	private Cluster.MemberType type;
	
	@Expose
	@Valid
	private ServerConfig server;
	
	public ClusterConfig()
	{
		server = new ServerConfig();
	}
	
	public Cluster.MemberType getType()
	{
		return type;
	}
	
	public ServerConfig getServer()
	{
		return server;
	}
}
