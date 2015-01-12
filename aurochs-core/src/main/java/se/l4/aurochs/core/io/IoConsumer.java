package se.l4.aurochs.core.io;

import java.io.IOException;

public interface IoConsumer<T>
{
	void accept(T item)
		throws IOException;
}
