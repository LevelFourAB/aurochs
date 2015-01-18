package se.l4.aurochs.core.io;

import java.io.Closeable;
import java.io.DataOutput;
import java.io.IOException;

public interface ExtendedDataOutput
	extends DataOutput, Closeable
{
	void writeVInt(int i)
		throws IOException;
	
	void writeVLong(long l)
		throws IOException;
	
	void writeString(String string)
		throws IOException;
	
	void writeBytes(Bytes bytes)
		throws IOException;
}
