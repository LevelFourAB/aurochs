package se.l4.aurochs.cluster.internal;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.l4.aurochs.cluster.Cluster;
import se.l4.aurochs.cluster.ServiceBuilder;
import se.l4.aurochs.cluster.nodes.Node;
import se.l4.aurochs.config.Config;
import se.l4.aurochs.core.Session;
import se.l4.aurochs.core.channel.CombiningChannel;
import se.l4.aurochs.core.hosts.Hosts;
import se.l4.aurochs.core.internal.NamedChannelCodec;
import se.l4.aurochs.core.io.ByteMessage;
import se.l4.aurochs.core.io.Bytes;
import se.l4.aurochs.core.io.ExtendedDataInput;
import se.l4.aurochs.net.RemoteSession;
import se.l4.aurochs.net.Server;
import se.l4.aurochs.net.ServerBuilder;
import se.l4.aurochs.net.ServerConnection;
import se.l4.crayon.services.ManagedService;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class ClusterImpl
	implements Cluster, ManagedService
{
	private static final Logger log = LoggerFactory.getLogger(Cluster.class);
	
	private final Provider<ServerBuilder> serverBuilders;
	private final Provider<ServerConnection> connections;
	
	private final ClusterConfig config;
	private final Map<Session, Node<ByteMessage>> sessionToNode;

	private MutableNodes<ByteMessage> coreNodes; 
	private Server server;

	@Inject
	public ClusterImpl(Config config, Provider<ServerBuilder> serverBuilders, Provider<ServerConnection> connections)
	{
		this.serverBuilders = serverBuilders;
		this.connections = connections;
		this.config = config.get("cluster", ClusterConfig.class)
			.getOrDefault(new ClusterConfig());
		
		sessionToNode = new ConcurrentHashMap<>();
	}
	
	@Override
	public void start()
		throws Exception
	{
		switch(config.getType())
		{
			case NONE:
				log.info("Not joining any cluster");
				return;
			case SERVER:
				log.info("Joining cluster as a full server member");
				server = serverBuilders.get()
					.withConfig(config.getServer())
					.withSessionListener(this::handleNewSession, this::handleRemoveSession)
					.start();
				break;
			case CLIENT:
				log.warn("Joining as a client not yet supported");
				return;
		}
		
		Hosts hosts = config.getHosts();
		if(hosts == null)
		{
			throw new RuntimeException("Unable to join cluster; no hosts specified in config");
		}

		String self;
		try
		{
			String selfId = config.getSelf();
			URI uri = new URI(selfId.contains("://") ? selfId : "aurochs://" + selfId);
			self = uri.getHost() + ":" + (uri.getPort() == -1 ? ServerConnection.DEFAULT_PORT : uri.getPort());
			if(! hosts.list().contains(uri))
			{
				throw new RuntimeException("Could not find self in the specified cluster hosts; Looked for " + self + " in " + hosts.list());
			}
			
			int serverPort = config.getServer().getPort();
			if((uri.getPort() == -1 && serverPort != ServerConnection.DEFAULT_PORT) || (uri.getPort() != -1 && serverPort != uri.getPort()))
			{
				throw new RuntimeException("The specified id " + selfId + " does not match the port number of the server (" + serverPort + ")");
			}
		}
		catch(URISyntaxException e)
		{
			throw new RuntimeException("Id of self must be in the form of a hostname and an optional IP");
		}
		
		
		coreNodes = new MutableNodes<>();
		hosts.listen(event -> {
			
			URI uri = event.getUri();
			String id = uri.getHost() + ":" + (uri.getPort() == -1 ? ServerConnection.DEFAULT_PORT : uri.getPort());
			if(! id.equals(self))
			{
				RemoteSession session = connections.get()
					.setHost(event.getUri())
					.setChannelInitializer(ch -> ch.transform(new NamedChannelCodec("cluster:joiner"))
						.send(Bytes.create(out -> out.writeUTF(self))
					))
					.connect();
				
				CombiningChannel<ByteMessage> channel = new CombiningChannel<>();
				channel.addChannel(session.getRawChannel());
				coreNodes.addNode(new Node<>(id, channel));
			}
			else
			{
				coreNodes.addNode(new Node<>(id, new CombiningChannel<>()));
			}
		});
		
		new ClusterCoordinator(coreNodes, self);
	}
	
	@Override
	public void stop()
		throws Exception
	{
		// TODO Auto-generated method stub
		
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void handleNewSession(Session session)
	{
		session.getNamedChannel("cluster:joiner").addListener(event -> {
			try(ExtendedDataInput in = event.getMessage().asDataInput())
			{
				String id = in.readUTF();
				Node<ByteMessage> node = coreNodes.get(id);
				if(node != null)
				{
					((CombiningChannel) node.getChannel()).addChannel(session.getRawChannel());
					sessionToNode.put(session, node);
				}
			}
			catch(IOException e)
			{
				throw new RuntimeException("Could not read message; " + e.getMessage(), e);
			}
		});
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void handleRemoveSession(Session session)
	{
		Node<ByteMessage> node = sessionToNode.remove(session);
		if(node == null) return;
		
		((CombiningChannel) node.getChannel()).removeChannel(session.getRawChannel());
	}
	
	@Override
	public MemberType getLocalType()
	{
		return config.getType();
	}
	
	@Override
	public ServiceBuilder<Bytes> newService(String name)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String toString()
	{
		return "Node "+ config.getSelf() + " (" + config.getType() + ")" + (config.getType() == MemberType.SERVER ? " - server at " + config.getServer().getPort(): "");
	}
}
