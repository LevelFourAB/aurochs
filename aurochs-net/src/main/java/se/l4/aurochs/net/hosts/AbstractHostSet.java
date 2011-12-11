package se.l4.aurochs.net.hosts;

import java.net.URI;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.common.collect.ImmutableSet;

/**
 * Abstract implementation of {@link HostSet}.
 * 
 * @author Andreas Holstenson
 *
 */
public abstract class AbstractHostSet
	implements HostSet
{
	private final CopyOnWriteArrayList<Listener> listeners;
	
	private volatile Set<URI> value;
	
	public AbstractHostSet()
	{
		listeners = new CopyOnWriteArrayList<Listener>();
	}
	
	protected void setHosts(Collection<URI> uris)
	{
		Set<URI> value = ImmutableSet.copyOf(uris);
		this.value = value;
		
		for(Listener l : listeners)
		{
			l.hostsChanged(value);
		}
	}
	
	@Override
	public Set<URI> list()
	{
		return value;
	}
	
	@Override
	public void addListener(Listener listener)
	{
		listeners.add(listener);
	}
	
	@Override
	public void removeListener(Listener listener)
	{
		listeners.remove(listener);
	}
}
