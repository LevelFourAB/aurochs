package se.l4.aurochs.cluster.internal;

import java.io.File;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.bval.constraints.NotEmpty;

import se.l4.aurochs.cluster.Cluster;
import se.l4.aurochs.core.hosts.Hosts;
import se.l4.aurochs.net.internal.ServerConfig;
import se.l4.commons.serialization.Expose;
import se.l4.commons.serialization.ReflectionSerializer;
import se.l4.commons.serialization.Use;
import se.l4.commons.serialization.enums.IgnoreCaseNameTranslator;
import se.l4.commons.serialization.enums.MapEnumVia;

@Use(ReflectionSerializer.class)
public class ClusterConfig
{
	@Expose
	@NotNull
	@MapEnumVia(IgnoreCaseNameTranslator.class)
	private Cluster.MemberType type;

	@Expose
	@NotNull
	@NotEmpty
	private String self;
	
	@Expose
	private Hosts hosts;
	
	@Expose
	@NotNull
	private File storage;
	
	@Expose
	@Valid
	private ServerConfig server;
	
	public ClusterConfig()
	{
		type = Cluster.MemberType.CLIENT;
		server = new ServerConfig();
		storage = new File("cluster");
	}
	
	public String getSelf()
	{
		return self;
	}
	
	public Cluster.MemberType getType()
	{
		return type;
	}
	
	public Hosts getHosts()
	{
		return hosts;
	}
	
	public File getStorage()
	{
		return storage;
	}
	
	public ServerConfig getServer()
	{
		return server;
	}
}
