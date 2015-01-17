package se.l4.aurochs.cluster.internal.partitions;

import java.io.File;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

import se.l4.aurochs.cluster.StateLog;
import se.l4.aurochs.cluster.StateLogBuilder;
import se.l4.aurochs.cluster.internal.raft.RaftBuilder;
import se.l4.aurochs.cluster.nodes.Nodes;
import se.l4.aurochs.cluster.partitions.PartitionChannel;
import se.l4.aurochs.cluster.partitions.PartitionCreateEncounter;
import se.l4.aurochs.cluster.partitions.PartitionService;
import se.l4.aurochs.core.channel.ChannelCodec;
import se.l4.aurochs.core.internal.NamedChannelCodec;
import se.l4.aurochs.core.io.ByteMessage;
import se.l4.aurochs.core.io.Bytes;
import se.l4.aurochs.core.io.IoConsumer;

import com.google.common.collect.Maps;

public class LocalPartition
{
	private final int partition;
	
	private final String self;
	private final File dataRoot;
	private final Nodes<ByteMessage> nodes;
	
	private final Map<String, PartitionServiceHandle> services;

	private final Partitions<ByteMessage> partitions;

	
	public LocalPartition(
			String self,
			Partitions<ByteMessage> partitions,
			int partition,
			File dataRoot)
	{
		this.self = self;
		this.partitions = partitions;
		this.partition = partition;
		this.dataRoot = dataRoot;
		this.nodes = partitions.getNodes(partition);
		services = Maps.newHashMap();
	}
	
	public <T> void startService(PartitionServiceRegistration<T> registration)
	{
		Encounter<T> encounter = new Encounter<>(registration.getName(), registration.getChannel());
		PartitionService<T> service = registration.getFactory().apply(encounter);
		
		services.put(registration.getName(), new PartitionServiceHandle(service, encounter.stateLogNodes, encounter.stateLog));
	}
	
	public void stopService(String name)
	{
		PartitionServiceHandle handle = services.get(name);
		if(handle == null) return;
		
		handle.stop();
	}
	
	private class Encounter<T>
		implements PartitionCreateEncounter<T>
	{
		private final File file;
		private final String name;
		private final PartitionChannel<T> channel;
		
		private Nodes<Bytes> stateLogNodes;
		private StateLog<Bytes> stateLog;

		public Encounter(String name, PartitionChannel<T> channel)
		{
			this.name = name;
			this.channel = channel;
			file = new File(dataRoot, name);
			getDataDir().mkdirs();
		}
		
		@Override
		public File getDataDir()
		{
			return new File(file, "data");
		}
		
		@Override
		public PartitionChannel<T> createChannel(Function<T, CompletableFuture<T>> messageHandler)
		{
			return ((ServicePartitionChannel<T>) channel).forPartition(partition, messageHandler);
		}
		
		@Override
		public StateLogBuilder<Bytes> stateLog()
		{
			stateLogNodes = nodes.transform(new NamedChannelCodec("p" + partition + ":" + name + ":state-log"));
			return new DelegatingStateLogBuilder<>(
				new RaftBuilder<Bytes>()
					.withNodes(stateLogNodes, self)
					.stateInFile(new File(file, "state-log")),
				log -> stateLog = log
			);
		}
	}
	
	private static class DelegatingStateLogBuilder<T>
		implements StateLogBuilder<T>
	{
		private Consumer<StateLog<T>> finisher;
		private StateLogBuilder<T> actualBuilder;

		public DelegatingStateLogBuilder(StateLogBuilder<T> actualBuilder, Consumer<StateLog<T>> finisher)
		{
			this.actualBuilder = actualBuilder;
			this.finisher = finisher;
		}
		
		@Override
		public StateLogBuilder<T> withNodes(Nodes<Bytes> nodes, String selfId)
		{
			actualBuilder = actualBuilder.withNodes(nodes, selfId);
			return this;
		}
		
		@Override
		public StateLogBuilder<T> stateInFile(File file)
		{
			actualBuilder = actualBuilder.stateInFile(file);
			return this;
		}
		
		@Override
		public StateLogBuilder<T> inMemory()
		{
			actualBuilder = actualBuilder.inMemory();
			return this;
		}
		
		@Override
		public <O> StateLogBuilder<O> transform(ChannelCodec<Bytes, O> codec)
		{
			actualBuilder = (StateLogBuilder) actualBuilder.transform(codec);
			return (StateLogBuilder) this;
		}
		
		@Override
		public StateLogBuilder<T> withApplier(IoConsumer<T> applier)
		{
			actualBuilder = actualBuilder.withApplier(applier);
			return this;
		}
		
		@Override
		public StateLogBuilder<T> withVolatileApplier(IoConsumer<T> applier)
		{
			actualBuilder = actualBuilder.withVolatileApplier(applier);
			return this;
		}
		
		@Override
		public StateLog<T> build()
		{
			StateLog<T> built = actualBuilder.build();
			finisher.accept(built);
			return built;
		}
	}
}