package se.l4.aurochs.cluster.internal.raft;

import java.io.IOException;
import java.util.List;

import se.l4.aurochs.cluster.internal.raft.log.DefaultLogEntry;
import se.l4.aurochs.cluster.internal.raft.log.LogEntry;
import se.l4.aurochs.cluster.internal.raft.messages.AppendEntries;
import se.l4.aurochs.cluster.internal.raft.messages.AppendEntriesReply;
import se.l4.aurochs.cluster.internal.raft.messages.RaftMessage;
import se.l4.aurochs.cluster.internal.raft.messages.RequestVote;
import se.l4.aurochs.cluster.internal.raft.messages.RequestVoteReply;
import se.l4.aurochs.core.channel.ChannelCodec;
import se.l4.aurochs.core.io.Bytes;
import se.l4.aurochs.core.io.ExtendedDataInput;
import se.l4.aurochs.core.io.ExtendedDataOutput;

import com.google.common.collect.Lists;

/**
 * {@link ChannelCodec} for decoding/encoding {@link RaftMessage}s.
 * 
 * @author Andreas Holstenson
 *
 */
public class RaftChannelCodec
	implements ChannelCodec<Bytes, RaftMessage>
{
	@Override
	public boolean accepts(Bytes in)
	{
		return in instanceof Bytes;
	}
	
	@Override
	public RaftMessage fromSource(Bytes object)
	{
		try(ExtendedDataInput in = object.asDataInput())
		{
			int tag = in.readByte();
			String sender = in.readUTF();
			long term = in.readVLong();
			switch(tag)
			{
				case 1:
					return new RequestVote(term, sender, in.readVLong(), in.readVLong());
				case 2:
					return new RequestVoteReply(sender, term, in.readBoolean());
				case 3:
					long prevLogIndex = in.readVLong();
					long prevLogTerm = in.readVLong();
					long leaderCommit = in.readVLong();
					int entryCount = in.readVInt();
					
					List<LogEntry> entries = Lists.newArrayListWithCapacity(entryCount);
					for(int i=0; i<entryCount; i++)
					{
						long entryIndex = in.readVLong();
						long entryTerm = in.readVLong();
						Bytes bytes = in.readBytes();
						entries.add(new DefaultLogEntry(entryIndex, entryTerm, bytes));
					}
					
					return new AppendEntries(sender, term, prevLogIndex, prevLogTerm, entries, leaderCommit);
				case 4:
					return new AppendEntriesReply(
						sender, term,
						in.readVLong(), in.readVInt(),
						in.readBoolean()
					);
				default:
					throw new RaftException("Unable to read message with tag " + tag);
			}
		}
		catch(IOException e)
		{
			throw new RaftException("Unable to deserialize; " + e.getMessage(), e);
		}
	}
	
	@Override
	public Bytes toSource(RaftMessage object)
	{
		return Bytes.create(out -> writeObject(out, object));
	}
	
	private void writeObject(ExtendedDataOutput out, RaftMessage object)
		throws IOException
	{
		if(object instanceof RequestVote)
		{
			RequestVote msg = (RequestVote) object;
			out.write(1);
			out.writeUTF(object.getSenderId());
			out.writeVLong(object.getTerm());
			out.writeVLong(msg.getLastLogIndex());
			out.writeVLong(msg.getLastLogTerm());
		}
		else if(object instanceof RequestVoteReply)
		{
			RequestVoteReply msg = (RequestVoteReply) object;
			out.write(2);
			out.writeUTF(object.getSenderId());
			out.writeVLong(object.getTerm());
			out.writeBoolean(msg.isVoteGranted());
		}
		else if(object instanceof AppendEntries)
		{
			AppendEntries msg = (AppendEntries) object;
			out.write(3);
			out.writeUTF(object.getSenderId());
			out.writeVLong(object.getTerm());
			out.writeVLong(msg.getPrevLogIndex());
			out.writeVLong(msg.getPrevLogTerm());
			out.writeVLong(msg.getLeaderCommit());
			
			out.writeVInt(msg.getEntries().size());
			for(LogEntry e : msg.getEntries())
			{
				out.writeVLong(e.getId());
				out.writeVLong(e.getTerm());
				out.writeBytes(e.getData());
			}
		}
		else if(object instanceof AppendEntriesReply)
		{
			AppendEntriesReply msg = (AppendEntriesReply) object;
			out.write(4);
			out.writeUTF(object.getSenderId());
			out.writeVLong(object.getTerm());
			out.writeVLong(msg.getPrevLogIndex());
			out.writeVInt(msg.getEntries());
			out.writeBoolean(msg.isSuccess());
		}
	}
}
