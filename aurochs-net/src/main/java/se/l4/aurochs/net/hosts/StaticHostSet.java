package se.l4.aurochs.net.hosts;

import java.net.URI;
import java.util.Collection;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * Set of hosts that is static.
 * 
 * @author Andreas Holstenson
 *
 */
public class StaticHostSet
	implements HostSet
{
	private final Set<URI> value;

	public StaticHostSet(URI... hosts)
	{
		value = ImmutableSet.copyOf(hosts);
	}
	
	public StaticHostSet(Collection<URI> hosts)
	{
		value = ImmutableSet.copyOf(hosts);
	}
	
	@Override
	public Set<URI> list()
	{
		return value;
	}
	
	@Override
	public void addListener(Listener listener)
	{
	}
	
	public void removeListener(Listener listener)
	{
	}
}
