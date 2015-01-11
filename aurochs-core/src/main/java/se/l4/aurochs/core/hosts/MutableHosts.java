package se.l4.aurochs.core.hosts;

import java.net.URI;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

import se.l4.aurochs.core.events.EventHandle;
import se.l4.aurochs.core.events.EventHelper;

public class MutableHosts
	implements Hosts
{
	private final Set<URI> hosts;
	private final EventHelper<Consumer<HostEvent>> events;
	
	public MutableHosts()
	{
		hosts = new CopyOnWriteArraySet<>();
		events = new EventHelper<>();
	}
	
	/**
	 * Add a new host.
	 * 
	 * @param uri
	 */
	public void add(URI uri)
	{
		if(hosts.add(uri))
		{
			HostEvent event = new HostEvent(HostEvent.Type.ADDED, uri);
			events.forEach(l -> l.accept(event));
		}
	}
	
	/**
	 * Remove a host.
	 * 
	 * @param uri
	 */
	public void remove(URI uri)
	{
		if(hosts.remove(uri))
		{
			HostEvent event = new HostEvent(HostEvent.Type.REMOVED, uri);
			events.forEach(l -> l.accept(event));
		}
	}
	
	@Override
	public Collection<URI> list()
	{
		return hosts;
	}
	
	@Override
	public EventHandle listen(Consumer<HostEvent> consumer)
	{
		for(URI uri : hosts)
		{
			consumer.accept(new HostEvent(HostEvent.Type.INITIAL, uri));
		}
		return events.listen(consumer);
	}
}
