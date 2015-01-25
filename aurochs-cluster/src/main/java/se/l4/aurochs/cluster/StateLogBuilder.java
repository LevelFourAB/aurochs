package se.l4.aurochs.cluster;

import java.io.File;
import java.util.function.Function;

import se.l4.aurochs.cluster.nodes.NodeSet;
import se.l4.aurochs.core.channel.ChannelCodec;
import se.l4.aurochs.core.io.Bytes;
import se.l4.aurochs.core.io.IoConsumer;

public interface StateLogBuilder<T>
{
	/**
	 * Set the nodes that this state log should be built over and specify the
	 * id of ourselves.
	 * 
	 * @param nodes
	 * @param selfId
	 * @return
	 */
	StateLogBuilder<T> withNodes(NodeSet<Bytes> nodes, String selfId);
	
	/**
	 * Transform the data the state log handles.
	 * 
	 * @param codec
	 * @return
	 */
	<O> StateLogBuilder<O> transform(ChannelCodec<Bytes, O> codec);
	
	default <O> StateLogBuilder<O> transform(Function<Bytes, O> from, Function<O, Bytes> to)
	{
		return transform(ChannelCodec.create(from, to));
	}
	
	/**
	 * Set the consumer that is used to commit entries in the log. The applier
	 * will be called at most once for every successful commit. If the
	 * log is closed and then opened again the applier will not be called for
	 * previous log entries.
	 * 
	 * <p>
	 * If your service has a in-memory representation of data use
	 * {@link #withVolatileApplier(IoConsumer)} instead.
	 * 
	 * @param applier
	 * @return
	 */
	StateLogBuilder<T> withApplier(IoConsumer<T> applier);
	
	/**
	 * Set the consumer that is used to commit entries in the log. This is
	 * similar to {@link #withApplier(IoConsumer)} with the change that this
	 * applier will be called for all previous log entries when the log is
	 * opened.
	 *  
	 * @param applier
	 * @return
	 */
	StateLogBuilder<T> withVolatileApplier(IoConsumer<T> applier);
	
	/**
	 * Request that the log is stored in memory.
	 * 
	 * @return
	 */
	StateLogBuilder<T> inMemory();
	
	/**
	 * Store state in a file.
	 * 
	 * @param file
	 * @return
	 */
	StateLogBuilder<T> stateInFile(File file);
	
	StateLog<T> build();
}
