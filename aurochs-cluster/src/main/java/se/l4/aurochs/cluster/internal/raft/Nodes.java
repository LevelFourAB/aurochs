package se.l4.aurochs.cluster.internal.raft;

import java.util.function.Consumer;

public interface Nodes
{
	void listen(Consumer<NodeEvent<Object>> consumer);
}
