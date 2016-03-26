package se.l4.aurochs.cluster.internal.partitions;

import java.io.File;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.IntObjectMap;

import se.l4.aurochs.cluster.partitions.PartitionMessage;
import se.l4.commons.io.ByteMessage;
import se.l4.commons.serialization.SerializerCollection;


/**
 * Coordinator for the partitions this local node is a member of.
 * 
 * @author Andreas Holstenson
 *
 */
public class LocalPartitionsCoordinator
{
	private final SerializerCollection serializers;
	
	private final Partitions<ByteMessage> partitions;
	private final IntObjectMap<LocalPartition> local;
	private volatile String self;
	private final File clusterData;
	
	private final List<PartitionServiceRegistration<?>> registrations;

	public LocalPartitionsCoordinator(SerializerCollection serializers, File clusterData, Partitions<ByteMessage> partitions)
	{
		this.serializers = serializers;
		this.self = self;
		this.clusterData = new File(clusterData, "partitions");
		this.partitions = partitions;
		
		registrations = new CopyOnWriteArrayList<>();
		
		local = new IntObjectHashMap<>();
	}
	
	public <T extends PartitionMessage> PartitionedServiceBuilderImpl<T> newBuilder(String name)
	{
		return new PartitionedServiceBuilderImpl<T>(serializers, partitions, name, registrations::add);
	}
	
	public void start(String self)
	{
		this.self = self;
		partitions.listen(event -> {
			if(event.getType() == PartitionEvent.Type.JOINED)
			{
				if(event.getNode().getId().equals(self))
				{
					join(event.getPartition());
				}
			}
			else if(event.getType() == PartitionEvent.Type.LEFT)
			{
				if(event.getNode().getId().equals(self))
				{
					leave(event.getPartition());
				}
			}
		});
	}
	
	private void join(int partition)
	{
		LocalPartition lp;
		synchronized(local)
		{
			lp = local.get(partition);
			if(lp == null)
			{
				File dataRoot = new File(clusterData, String.valueOf(partition));
				dataRoot.mkdirs();
				lp = new LocalPartition(self, partitions, partition, dataRoot);
				
				local.put(partition, lp);
			}
		}
		
		for(PartitionServiceRegistration<?> r : registrations)
		{
			lp.startService(r);
		}
	}
	
	private void leave(int partition)
	{
		LocalPartition lp;
		synchronized(local)
		{
			lp = local.get(partition);
			if(lp == null) return;
		}
		
		for(PartitionServiceRegistration<?> r : registrations)
		{
			// TODO: This should probably remove data as well
			lp.stopService(r.getName());
		}
	}
}
