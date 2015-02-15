package se.l4.aurochs.cluster.internal.chunkedstate;

public class DefaultChunk
	implements Chunk
{
	private final long request;
	private final ChunkType type;
	private final byte[] data;

	public DefaultChunk(long request, ChunkType type, byte[] data)
	{
		this.request = request;
		this.type = type;
		this.data = data;
	}

	@Override
	public long request()
	{
		return request;
	}

	@Override
	public ChunkType type()
	{
		return type;
	}

	@Override
	public byte[] data()
	{
		return data;
	}

}
