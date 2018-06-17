package se.l4.aurochs.hosts;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import com.google.common.collect.Lists;

import se.l4.commons.serialization.Serializer;
import se.l4.commons.serialization.format.StreamingInput;
import se.l4.commons.serialization.format.StreamingOutput;
import se.l4.commons.serialization.format.Token;

public class HostsSerializer
	implements Serializer<Hosts>
{

	@Override
	public Hosts read(StreamingInput in)
		throws IOException
	{
		in.next(Token.LIST_START);
		List<URI> uris =  Lists.newArrayList();
		while(in.peek() != Token.LIST_END)
		{
			in.next(Token.VALUE);
			String uri = in.getString();
			if(! uri.contains("://"))
			{
				uri = "aurochs://" + uri;
			}
			
			uris.add(URI.create(uri));
		}
		in.next(Token.LIST_END);
		return Hosts.create(uris);
	}

	@Override
	public void write(Hosts object, String name, StreamingOutput stream)
		throws IOException
	{
		stream.writeListStart(name);
		for(URI uri : object.list())
		{
			stream.write("e", uri.toString());
		}
		stream.writeListEnd(name);
	}

}
