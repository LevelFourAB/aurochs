package se.l4.aurochs.cluster.internal.partitions;

import se.l4.aurochs.cluster.partitions.Partitioner;

public class PartitionerImpl
	implements Partitioner
{
	private final Partitions<?> partitions;
	private final int seed;

	public PartitionerImpl(Partitions<?> partitions)
	{
		this.partitions = partitions;
		seed = 158910246;
	}
	
	@Override
	public int partition(byte[] data)
	{
		return hash(data) % partitions.getTotal();
	}
	
	@Override
	public int partition(long id)
	{
		return hash(id) % partitions.getTotal();
	}
	
	@Override
	public int partition(String id)
	{
		return hash(id) % partitions.getTotal();
	}
	

	private int hash(String string)
	{
		final int c1 = 0xcc9e2d51;
		final int c2 = 0x1b873593;

		int h1 = seed;

		int pos = 0;
		int end = string.length();
		int k1 = 0;
		int k2 = 0;
		int shift = 0;
		int bits = 0;
		int nBytes = 0;

		while(pos < end)
		{
			int code = string.charAt(pos++);
			if(code < 0x80)
			{
				k2 = code;
				bits = 8;
			}
			else if(code < 0x800)
			{
				k2 = (0xC0 | (code >> 6)) 
					| ((0x80 | (code & 0x3F)) << 8);
				bits = 16;
			}
			else if (code < 0xD800 || code > 0xDFFF || pos >= end)
			{
				k2 = (0xE0 | (code >> 12))
					| ((0x80 | ((code >> 6) & 0x3F)) << 8)
					| ((0x80 | (code & 0x3F)) << 16);
				bits = 24;
			}
			else
			{
				int utf32 = string.charAt(pos++);
				utf32 = ((code - 0xD7C0) << 10) + (utf32 & 0x3FF);
				k2 = (0xff & (0xF0 | (utf32 >> 18)))
					| ((0x80 | ((utf32 >> 12) & 0x3F))) << 8
					| ((0x80 | ((utf32 >> 6) & 0x3F))) << 16
					| (0x80 | (utf32 & 0x3F)) << 24;
				bits = 32;
			}

			k1 |= k2 << shift;

			shift += bits;
			if(shift >= 32)
			{
				k1 *= c1;
				k1 = (k1 << 15) | (k1 >>> 17);
				k1 *= c2;

				h1 ^= k1;
				h1 = (h1 << 13) | (h1 >>> 19);
				h1 = h1 * 5 + 0xe6546b64;

				shift -= 32;
				if(shift != 0)
				{
					k1 = k2 >>> (bits - shift);
				}
				else
				{
					k1 = 0;
				}
				nBytes += 4;
			}

		}

		if(shift > 0)
		{
			nBytes += shift >> 3;
			k1 *= c1;
			k1 = (k1 << 15) | (k1 >>> 17);
			k1 *= c2;
			h1 ^= k1;
		}

		h1 ^= nBytes;

		h1 ^= h1 >>> 16;
		h1 *= 0x85ebca6b;
		h1 ^= h1 >>> 13;
		h1 *= 0xc2b2ae35;
		h1 ^= h1 >>> 16;

		return h1;
	}

	private int hash(byte[] data)
	{
		final int c1 = 0xcc9e2d51;
		final int c2 = 0x1b873593;

		int h1 = seed;
		int roundedEnd = (data.length & 0xfffffffc);

		for(int i=0; i<roundedEnd; i+=4)
		{
			int k1 = (data[i] & 0xff)
				| ((data[i + 1] & 0xff) << 8)
				| ((data[i + 2] & 0xff) << 16)
				| (data[i + 3] << 24);
			k1 *= c1;
			k1 = (k1 << 15) | (k1 >>> 17);
			k1 *= c2;

			h1 ^= k1;
			h1 = (h1 << 13) | (h1 >>> 19);
			h1 = h1 * 5 + 0xe6546b64;
		}

		int k1 = 0;

		switch(data.length & 0x03)
		{
			case 3:
				k1 = (data[roundedEnd + 2] & 0xff) << 16;
			case 2:
				k1 |= (data[roundedEnd + 1] & 0xff) << 8;
			case 1:
				k1 |= (data[roundedEnd] & 0xff);
				k1 *= c1;
				k1 = (k1 << 15) | (k1 >>> 17); // ROTL32(k1,15);
				k1 *= c2;
				h1 ^= k1;
		}

		h1 ^= data.length;

		h1 ^= h1 >>> 16;
		h1 *= 0x85ebca6b;
		h1 ^= h1 >>> 13;
		h1 *= 0xc2b2ae35;
		h1 ^= h1 >>> 16;

		return h1;
	}

	private int hash(long in)
	{
		int low = (int) in;
	    int high = (int) (in >>> 32);

	    int k1 = mixK1(low);
	    int h1 = mixH1(seed, k1);

	    k1 = mixK1(high);
	    h1 = mixH1(h1, k1);
	    
	    h1 ^= 8;
	    h1 ^= h1 >>> 16;
	    h1 *= 0x85ebca6b;
	    h1 ^= h1 >>> 13;
	    h1 *= 0xc2b2ae35;
	    h1 ^= h1 >>> 16;
	    
	    return h1;
	}
	
	private static int mixK1(int k1)
	{
		k1 *= 0xcc9e2d51;
		k1 = Integer.rotateLeft(k1, 15);
		k1 *= 0x1b873593;
		return k1;
	}

	private static int mixH1(int h1, int k1)
	{
		h1 ^= k1;
		h1 = Integer.rotateLeft(h1, 13);
		h1 = h1 * 5 + 0xe6546b64;
		return h1;
	}
}
