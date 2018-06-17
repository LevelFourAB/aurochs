package se.l4.aurochs.statelog;

import java.io.IOException;

/**
 * Implementation of {@link LogEntry}.
 * 
 * @author Andreas Holstenson
 *
 * @param <T>
 */
public class DefaultLogEntry<T>
	implements LogEntry<T>
{
	private final long id;
	private final Type type;
	private final T data;

	public DefaultLogEntry(long id, Type type, T data)
	{
		this.id = id;
		this.type = type;
		this.data = data;
	}
	
	@Override
	public long id()
	{
		return id;
	}
	
	@Override
	public Type type()
	{
		return type;
	}
	
	@Override
	public T data()
		throws IOException
	{
		return data;
	}
}
