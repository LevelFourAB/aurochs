package se.l4.aurochs.cluster.internal.raft;

import java.util.List;
import java.util.Map;

import se.l4.aurochs.cluster.internal.StaticNodes;
import se.l4.aurochs.cluster.internal.raft.log.InMemoryLog;
import se.l4.aurochs.cluster.nodes.Node;
import se.l4.aurochs.core.channel.LocalChannel;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class LocalRaft
{
	private LocalRaft()
	{
	}
	
	public static Map<String, Raft> createRafts(String... allNodes)
	{
		Map<String, LocalChannel<Object>> channels = Maps.newHashMap();
		for(String n : allNodes)
		{
			channels.put(n, LocalChannel.create());
		}
		
		ImmutableMap.Builder<String, Raft> result = ImmutableMap.builder();
		for(String node : allNodes)
		{
			List<Node<Object>> nodes = Lists.newArrayList();
			for(String n : allNodes)
			{
				LocalChannel<Object> local = channels.get(n);
				nodes.add(new Node<>(n, n.equals(node) ? local.getIncoming() : local.getOutgoing()));
			}
			
			result.put(node, new Raft(new InMemoryStateStorage(), new InMemoryLog(), new StaticNodes(nodes), node));
		}
		
		return result.build();
	}
}
