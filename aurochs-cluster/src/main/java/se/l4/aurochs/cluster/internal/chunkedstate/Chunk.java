package se.l4.aurochs.cluster.internal.chunkedstate;


public interface Chunk
{
	long request();
	
	ChunkType type();
	
	byte[] data();
}