package se.l4.aurochs.core.id;

import java.util.concurrent.ThreadLocalRandom;

import com.google.inject.Singleton;

@Singleton
public class SimpleLongIdGenerator
	implements LongIdGenerator
{
	/** Start of this generators epoch, 2013-01-01 00:00 GMT */
	public static final long EPOCH_START = 1356998400000l;
	public static final int MAX_RANDOM = -1 ^ (-1 << 22);
	
	public SimpleLongIdGenerator()
	{
	}
	
	@Override
	public long next()
	{
		long time = System.currentTimeMillis() - EPOCH_START;
		long r = ThreadLocalRandom.current().nextLong() >>> 42;
		return (time << 22) | r;
	}
}
