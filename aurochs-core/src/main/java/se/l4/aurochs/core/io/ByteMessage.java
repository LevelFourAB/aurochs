package se.l4.aurochs.core.io;

import java.util.function.Predicate;

public interface ByteMessage
{
	int getTag();
	
	Bytes getData();
	
	static Predicate<ByteMessage> tag(int tag)
	{
		return (in) -> in.getTag() == tag;
	}
}
