package se.l4.aurochs.cluster.internal;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.l4.aurochs.cluster.Cluster;
import se.l4.aurochs.cluster.ServiceBuilder;
import se.l4.aurochs.cluster.internal.partitions.LocalPartitionsCoordinator;
import se.l4.aurochs.cluster.internal.partitions.MutablePartitions;
import se.l4.aurochs.cluster.internal.service.ServiceBuilderImpl;
import se.l4.aurochs.cluster.nodes.Node;
import se.l4.aurochs.config.Config;
import se.l4.aurochs.core.Session;
import se.l4.aurochs.core.channel.CombiningChannel;
import se.l4.aurochs.core.channel.LocalChannel;
import se.l4.aurochs.core.hosts.Hosts;
import se.l4.aurochs.core.internal.NamedChannelCodec;
import se.l4.aurochs.core.io.ByteMessage;
import se.l4.aurochs.core.io.Bytes;
import se.l4.aurochs.core.io.ExtendedDataInput;
import se.l4.aurochs.net.RemoteSession;
import se.l4.aurochs.net.Server;
import se.l4.aurochs.net.ServerBuilder;
import se.l4.aurochs.net.ServerConnection;
import se.l4.aurochs.serialization.SerializerCollection;
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

	private final SerializerCollection serializers;
	
	private final MutablePartitions<ByteMessage> partitions;
	private final LocalPartitionsCoordinator partitionCoordinator;
	
	private MutableNodes<ByteMessage> coreNodes; 
	private Server server;

	@Inject
	public ClusterImpl(Config config,
			Provider<ServerBuilder> serverBuilders,
			Provider<ServerConnection> connections, 
			SerializerCollection serializers)
	{
		this.serverBuilders = serverBuilders;
		this.connections = connections;
		this.serializers = serializers;
		this.config = config.get("cluster", ClusterConfig.class)
			.getOrDefault(new ClusterConfig());
		
		sessionToNode = new ConcurrentHashMap<>();
		
		partitions = new MutablePartitions<>(7);
		partitionCoordinator = new LocalPartitionsCoordinator(serializers, this.config.getStorage(), partitions);
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
			throw new RuntimeException("Id of self must be in the form of a hostname and an optional port");
		}
		
		
		coreNodes = new MutableNodes<>();
		hosts.listen(event -> {
			
			URI uri = event.getUri();
			String id = uri.getHost() + ":" + (uri.getPort() == -1 ? ServerConnection.DEFAULT_PORT : uri.getPort());
			if(! id.equals(self))
			{
				RemoteSession control = connections.get()
					.setHost(event.getUri())
					.setChannelInitializer(ch -> ch.transform(new NamedChannelCodec("cluster:joiner"))
						.send(Bytes.lazyViaDataOutput(out -> {
							out.writeByte(1);
							out.writeString(self);
						})
					))
					.connect();
				
				CombiningChannel<ByteMessage> controlChannel = new CombiningChannel<>();
				controlChannel.addChannel(control.getRawChannel());
				
				RemoteSession data = connections.get()
					.setHost(event.getUri())
					.setChannelInitializer(ch -> ch.transform(new NamedChannelCodec("cluster:joiner"))
						.send(Bytes.lazyViaDataOutput(out -> {
							out.writeByte(2);
							out.writeString(self);
						})
					))
//					.setMinConnections(4)
					.connect();
				
				CombiningChannel<ByteMessage> dataChannel = new CombiningChannel<>();
				dataChannel.addChannel(data.getRawChannel());
				
				coreNodes.addNode(new Node<>(id, controlChannel, controlChannel, dataChannel, dataChannel));
			}
			else
			{
				LocalChannel<ByteMessage> control = LocalChannel.create();
				LocalChannel<ByteMessage> data = LocalChannel.create();
				coreNodes.addNode(new Node<>(id, control.getIncoming(), control.getOutgoing(), data.getIncoming(), data.getOutgoing()));
			}
		});
		
		File storage = config.getStorage();
		storage.mkdirs();
		
		new ClusterCoordinator(coreNodes, self, storage);
		
		// Fire up a static partition table
		for(int i=0, n=partitions.getTotal(); i<n; i++)
		{
			for(Node<ByteMessage> node : coreNodes.list())
			{
				partitions.join(i, node);
			}
		}
		
		partitions.setLocal(coreNodes.get(self));
		
		partitionCoordinator.start(self);
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
				int channel = in.readUnsignedByte();
				String id = in.readString();
				Node<ByteMessage> node = coreNodes.get(id);
				if(node != null)
				{
					if(channel == 1)
					{
						((CombiningChannel) node.controlIncoming()).addChannel(session.getRawChannel());
					}
					else if(channel == 2)
					{
						((CombiningChannel) node.incoming()).addChannel(session.getRawChannel());
					}
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
		
		((CombiningChannel) node.controlIncoming()).removeChannel(session.getRawChannel());
		((CombiningChannel) node.incoming()).removeChannel(session.getRawChannel());
	}
	
	@Override
	public MemberType getLocalType()
	{
		return config.getType();
	}
	
	@Override
	public ServiceBuilder<Bytes> newService(String name)
	{
		return new ServiceBuilderImpl<>(name, partitionCoordinator);
	}
	
	@Override
	public String toString()
	{
		return "Node "+ config.getSelf() + " (" + config.getType() + ")" + (config.getType() == MemberType.SERVER ? " - server at " + config.getServer().getPort(): "");
	}
}
