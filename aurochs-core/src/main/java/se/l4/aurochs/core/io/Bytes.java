package se.l4.aurochs.core.io;

import java.io.IOException;
import java.io.InputStream;

public interface Bytes
{
	InputStream asInputStream()
		throws IOException;
	
	byte[] toByteArray()
		throws IOException;
	
	byte[] toByteArray(int offset, int length)
		throws IOException;

	static Bytes create(byte[] byteArray)
	{
		return new BytesOverByteArray(byteArray);
	}

}
