package se.l4.aurochs.core.hosts;

import java.net.URI;
import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

import se.l4.aurochs.core.events.EventHandle;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;

/**
 * Set of hosts that is static.
 * 
 * @author Andreas Holstenson
 *
 */
public class ImmutableHosts
	implements Hosts
{
	private final Set<URI> value;

	public ImmutableHosts(URI... hosts)
	{
		value = ImmutableSet.copyOf(hosts);
	}
	
	public ImmutableHosts(Collection<URI> hosts)
	{
		value = ImmutableSet.copyOf(hosts);
	}
	
	@Override
	public Collection<URI> list()
	{
		return value;
	}
	
	@Override
	public EventHandle listen(Consumer<HostEvent> consumer)
	{
		for(URI uri : value)
		{
			consumer.accept(new HostEvent(HostEvent.Type.INITIAL, uri));
		}
		
		return EventHandle.noop();
	}
	
	@Override
	public String toString()
	{
		return Objects.toStringHelper(this)
			.addValue(value)
			.toString();
	}
}
