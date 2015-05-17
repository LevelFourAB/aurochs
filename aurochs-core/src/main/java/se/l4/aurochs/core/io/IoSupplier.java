package se.l4.aurochs.core.io;

import java.io.IOException;

public interface IoSupplier<T>
{
	T get()
		throws IOException;
}
