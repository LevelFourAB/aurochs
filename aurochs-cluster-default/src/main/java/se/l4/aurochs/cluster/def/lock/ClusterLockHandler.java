package se.l4.aurochs.cluster.def.lock;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import se.l4.aurochs.cluster.partitions.PartitionCreateEncounter;
import se.l4.aurochs.cluster.partitions.PartitionService;
import se.l4.aurochs.core.channel.MessageEvent;
import se.l4.aurochs.core.io.Bytes;
import se.l4.aurochs.core.io.ExtendedDataInput;
import se.l4.aurochs.core.log.StateLog;

import com.google.common.base.Throwables;

public class ClusterLockHandler
	implements PartitionService<LockMessage>
{
	private final StateLog<LockState> stateLog;
	private final Map<String, LockState> locks;

	public ClusterLockHandler(PartitionCreateEncounter<LockMessage> encounter)
	{
		this.stateLog = encounter.stateLog()
			.transform(this::toLogObject, this::fromLogObject)
			.withVolatileApplier(this::receiveLogEvent)
			.build();
		
		locks = new ConcurrentHashMap<>();
	}
	
	@Override
	public void receiveMessage(MessageEvent<LockMessage> message)
	{
		if(message.getMessage() instanceof RequestLock)
		{
			handleRequestLock((RequestLock) message.getMessage());
		}
	}
	
	private void handleRequestLock(RequestLock message)
	{
		LockState state = locks.get(message.getLock());
		if(state == null || ! state.isLocked())
		{
			stateLog.submit(new LockState(
				message.getLock(),
				System.currentTimeMillis() + 3600 * 5
			));
		}
	}

	private void receiveLogEvent(LockState event)
	{
		
	}
	
	private LockState toLogObject(Bytes bytes)
	{
		try(ExtendedDataInput in = bytes.asDataInput())
		{
			String name = in.readUTF();
			long expiration = in.readVLong();
			
			return new LockState(name, expiration);
		}
		catch(IOException e)
		{
			throw Throwables.propagate(e);
		}
	}
	
	private Bytes fromLogObject(LockState state)
	{
		return Bytes.lazyViaDataOutput(out -> {
			out.writeUTF(state.name);
			out.writeVLong(state.expiration);
		});
	}
	
	private static class LockState
	{
		private final String name;
		private final long expiration;
		
		public LockState(String name, long expiration)
		{
			this.name = name;
			this.expiration = expiration;
		}
		
		public boolean isLocked()
		{
			return expiration < System.currentTimeMillis();
		}
	}
}
