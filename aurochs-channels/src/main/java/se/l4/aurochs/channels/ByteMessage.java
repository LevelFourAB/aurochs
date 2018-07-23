package se.l4.aurochs.channels;

import java.util.function.Predicate;

import se.l4.commons.io.Bytes;

public interface ByteMessage
{
	long getTag();

	Bytes getData();

	static Predicate<ByteMessage> tag(int tag)
	{
		return in -> in.getTag() == tag;
	}
}