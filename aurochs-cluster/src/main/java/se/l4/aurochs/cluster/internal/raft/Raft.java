package se.l4.aurochs.cluster.internal.raft;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.l4.aurochs.cluster.StateLog;
import se.l4.aurochs.cluster.internal.raft.log.DefaultLogEntry;
import se.l4.aurochs.cluster.internal.raft.log.Log;
import se.l4.aurochs.cluster.internal.raft.log.LogEntry;
import se.l4.aurochs.cluster.internal.raft.messages.AppendEntries;
import se.l4.aurochs.cluster.internal.raft.messages.AppendEntriesReply;
import se.l4.aurochs.cluster.internal.raft.messages.RaftMessage;
import se.l4.aurochs.cluster.internal.raft.messages.RequestVote;
import se.l4.aurochs.cluster.internal.raft.messages.RequestVoteReply;
import se.l4.aurochs.cluster.nodes.Node;
import se.l4.aurochs.cluster.nodes.NodeEvent;
import se.l4.aurochs.cluster.nodes.Nodes;
import se.l4.aurochs.core.channel.Channel;
import se.l4.aurochs.core.channel.ChannelListener;
import se.l4.aurochs.core.channel.MessageEvent;
import se.l4.aurochs.core.io.Bytes;
import se.l4.crayon.services.ManagedService;

import com.carrotsearch.hppc.LongObjectMap;
import com.carrotsearch.hppc.LongObjectOpenHashMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class Raft
	implements ManagedService, StateLog<Bytes>
{
	private static final long LOG_CACHE_SIZE = 10;

	private static final LogEntry LOG_HEAD = new DefaultLogEntry(0, 0, null);

	private final Logger logger;
	
	private final StateStorage stateStorage;
	private final Log log;
	
	private final ScheduledExecutorService scheduler;
	private final ExecutorService executor;
	
	private final ChannelListener<RaftMessage> channelListener;
	
	private final Random random;
	private final Lock stateLock;
	
	private final String id;
	
//	private final LinkedList<LogEntry> logCache;
	
	private final Map<String, Node<RaftMessage>> nodes;
	private final Node<RaftMessage> self;
	
	private ScheduledFuture<?> electionTimeout;
	private ScheduledFuture<?> heartbeat;

	private Role role;
	private final Set<String> votes;

	private Node<RaftMessage> leader;
	
	private final Map<String, NodeState> nodeStates;
	
	private long commitIndex;
	private long lastApplied;
	
	private final LongObjectMap<CompletableFuture<Void>> futures;
	
	public Raft(StateStorage stateStorage, Log log, Nodes<RaftMessage> nodes, String id)
	{
		this.logger = LoggerFactory.getLogger(Raft.class.getName() + "[" + id + "]");
		
		this.stateStorage = stateStorage;
		this.log = log;
		
		this.id = id;
		
		futures = new LongObjectOpenHashMap<>();
		
		ThreadFactory threadFactory = new ThreadFactoryBuilder()
			.setNameFormat("Raft[" + id + "] %s")
			.setUncaughtExceptionHandler(this::uncaughtException)
			.build();
		scheduler = Executors.newScheduledThreadPool(1, threadFactory);
		executor = Executors.newCachedThreadPool(threadFactory);
		
		this.stateLock = new ReentrantLock();
		random = new Random();
		
//		logCache = createLogCache(log);
		
		this.channelListener = createChannelListener();
		this.nodes = Maps.newHashMap();
		
		nodes.listen(this::handleNodeEvent);
		
		this.self = this.nodes.get(id);
		if(self == null)
		{
			throw new IllegalArgumentException("Nodes did not include self with id " + id);
		}
		
		nodeStates = Maps.newHashMap();
		votes = Sets.newHashSet();
		
		role = Role.FOLLOWER;
		scheduleElectionTimeout();
	}
	
	private void uncaughtException(Thread t, Throwable e)
	{
		logger.warn("Exception caught in background; " + e.getMessage(), e);
	}

	private ChannelListener<RaftMessage> createChannelListener()
	{
		return new ChannelListener<RaftMessage>()
		{
			@Override
			public void messageReceived(MessageEvent<RaftMessage> event)
			{
				RaftMessage message = event.getMessage();
				
				// Get the node this was sent from
				Node<RaftMessage> node = nodes.get(message.getSenderId());
				
				if(message instanceof RequestVote)
				{
					handleRequestVote(node, (RequestVote) message);
				}
				else if(message instanceof RequestVoteReply)
				{
					handleRequestVoteReply(node, (RequestVoteReply) message);
				}
				else if(message instanceof AppendEntries)
				{
					handleAppendEntries(node, (AppendEntries) message);
				}
				else if(message instanceof AppendEntriesReply)
				{
					handleAppendEntriesReply(node, (AppendEntriesReply) message);
				}
			}
		};
	}
	
	private void handleNodeEvent(NodeEvent<RaftMessage> event)
	{
		Node<RaftMessage> node = event.getNode();
		
		// TODO: Support transforming data in the channel
		Channel<RaftMessage> channel = node.getChannel()
			.filter(RaftMessage.class)
			.on(executor);
		
		channel.addListener(channelListener);
		
		Node<RaftMessage> transformed = new Node<>(node.getId(), channel);
		nodes.put(node.getId(), transformed);
	}
	
	@Override
	public void start()
		throws Exception
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void stop()
		throws Exception
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public CompletableFuture<Void> submit(Bytes entry)
	{
		stateLock.lock();
		try
		{
			if(leader == self)
			{
				return requestAppendEntry(entry);
			}
			else
			{
				return forwardAppendEntryToLeader(entry);
			}
		}
		finally
		{
			stateLock.unlock();
		}
	}
	
	private CompletableFuture<Void> requestAppendEntry(Bytes data)
	{
		stateLock.lock();
		try
		{
			long id = log.store(stateStorage.getCurrentTerm(), data);
			
			CompletableFuture<Void> future = new CompletableFuture<>();
			futures.put(id, future);
			
			// TODO: Should we send a heartbeat every time we do something?
			sendHeartbeat();
			
			return future;
		}
		catch(IOException e)
		{
			throw new RaftException("Unable to write to local log; " + e.getMessage(), e);
		}
		finally
		{
			stateLock.unlock();
		}
	}
	
	private CompletableFuture<Void> forwardAppendEntryToLeader(Bytes data)
	{
		return null;
	}
	
	/**
	 * Handler for when a candidate node asks us to vote for them.
	 * 
	 * @param node
	 * @param message
	 */
	private void handleRequestVote(Node<RaftMessage> node, RequestVote message)
	{
		logger.debug("Node {} requested us to vote in term {}", message.getCandidateId(), message.getTerm());
		stateLock.lock();
		try
		{
			long term = stateStorage.getCurrentTerm();
			
			if(term < message.getTerm())
			{
				switchRoleToFollower(message.getTerm());
			}
			else if(role == Role.LEADER) return;
			
			if(message.getTerm() < term)
			{
				logger.debug("This node has a higher term {}, voting no", term);
				sendTo(node, new RequestVoteReply(id, term, false));
				return;
			}
			
			long newTerm = message.getTerm();
			stateStorage.updateCurrentTerm(newTerm);
			
			String vote = stateStorage.getVote(newTerm);
			LogEntry lastLogEntry = getLastLogEntry();
			if((vote == null || vote.equals(node.getId())) && lastLogEntry.getId() <= message.getLastLogIndex())
			{
				logger.debug("First vote, voting yes to node {}", node.getId());
				stateStorage.updateVote(message.getTerm(), node.getId());
				sendTo(node, new RequestVoteReply(id, newTerm, true));
				return;
			}
			
			// Vote no to this node
			logger.debug("Already voted for {} instead of {}, voting no", vote, node.getId());
			sendTo(node, new RequestVoteReply(id, newTerm, false));
		}
		finally
		{
			stateLock.unlock();
		}
	}
	
	private void handleRequestVoteReply(Node<RaftMessage> node, RequestVoteReply reply)
	{
		stateLock.lock();
		try
		{
			long term = stateStorage.getCurrentTerm();
			if(term < reply.getTerm())
			{
				switchRoleToFollower(reply.getTerm());
				return;
			}
			
			if(reply.isVoteGranted())
			{
				logger.debug("Got a reply from {} who voted yes, currently {} votes yes", node.getId(), votes.size() + 1);
				
				votes.add(node.getId());
				if(role != Role.LEADER && isMajority(votes.size(), nodes.size()))
				{
					cancelElectionTimeout();
					
					role = Role.LEADER;
					leader = self;
					
					logger.debug("Became leader, due to having " + votes.size() + " votes of " + nodes.size() + " total");
					
					LogEntry lastLogEntry = getLastLogEntry();
					for(Node<RaftMessage> n : nodes.values())
					{
						nodeStates.put(n.getId(), new NodeState(lastLogEntry.getId() + 1));
					}
					
					sendHeartbeat();
					scheduleSendHeartbeat();
				}
			}
			else
			{
				logger.debug("Got a reply from {} who voted no, currently {} votes yes", node.getId(), votes.size());
			}
		}
		finally
		{
			stateLock.unlock();
		}
	}
	
	private boolean isMajority(int votes, int total)
	{
		return votes * 2 > total;
	}
	
	private void handleAppendEntries(Node<RaftMessage> node, AppendEntries message)
	{
		stateLock.lock();
		try
		{
			logger.debug("Got append entries from {} when in role {}", node.getId(), role);
			
			long term = stateStorage.getCurrentTerm();
			if(term < message.getTerm() || role == Role.CANDIDATE || (role == Role.FOLLOWER && leader == null))
			{
				// We switch ourselves to a follower in three cases:
				// 1) The term in the message is higher than ours
				// 2) We are a candidate
				// 3) We are a follower but we don't know who the leader is
				switchRoleToFollower(message.getTerm());
				
				// And update our local leader
				leader = node;
				
				term = message.getTerm();
			}
			else if(term > message.getTerm())
			{
				// Our term is more than the callers term, schedule an election and reply false
				logger.debug("Our term is {}, but the caller is in {}, ignoring append entries", term, message.getTerm());
				
				scheduleHearbeatElection();
				
				sendTo(node, new AppendEntriesReply(id, term, message.getPrevLogIndex(), message.getEntries().size(), false));
				return;
			}
			
			// Always schedule a new election
			scheduleHearbeatElection();
			
			LogEntry prevLog = getLogEntry(message.getPrevLogIndex());
			if(prevLog == null || prevLog.getTerm() != message.getPrevLogTerm())
			{
				if(logger.isDebugEnabled())
				{
					if(prevLog == null)
					{
						logger.debug("We do not have log entry at index " + message.getPrevLogIndex());
					}
					else
					{
						logger.debug("prevLogTerm was not same as ours (" + message.getPrevLogTerm() + " != " + prevLog.getTerm() + ") for " + message.getPrevLogIndex());
					}
				}
				
				sendTo(node, new AppendEntriesReply(id, term, message.getPrevLogIndex(), message.getEntries().size(), false));
				return;
			}
			
			logger.debug("About to append {} entries", message.getEntries().size());
			
			// Store all of the new entries
			for(LogEntry e : message.getEntries())
			{
				LogEntry previous = getLogEntry(e.getId());
				if(previous != null)
				{
					// This entry is already stored
					if(previous.getTerm() == e.getTerm())
					{
						logger.debug("Already have log entry at {} with same term, no need to apply", e.getId());
						// This is the same entry, no need to store it
						continue;
					}
					else
					{
						logger.debug("Log entry at {} has different term, resetting log", e.getId());
						
						// Remove this entry from the log and all following it
						log.resetTo(e.getId() - 1);
					}
				}
				
				logger.debug("Appending {} to log", e.getId());
				long index = log.store(e.getTerm(), e.getData());
			}
			
			if(message.getLeaderCommit() > commitIndex)
			{
				commitUpTo(Math.min(message.getLeaderCommit(), log.last()));
			}
			
			sendTo(node, new AppendEntriesReply(id, term, message.getPrevLogIndex(), message.getEntries().size(), true));
		}
		catch(IOException e)
		{
			throw new RaftException("Unable to update log; " + e.getMessage(), e);
		}
		finally
		{
			stateLock.unlock();
		}
	}
	
	private void handleAppendEntriesReply(Node<RaftMessage> node, AppendEntriesReply message)
	{
		stateLock.lock();
		try
		{
			logger.debug("Got append entries reply from {} with last applied {}", node.getId(), message.getPrevLogIndex() + message.getEntries());
			
			long term = stateStorage.getCurrentTerm();
			if(term > message.getTerm())
			{
				// Old reply?
				return;
			}
			else if(term < message.getTerm())
			{
				switchRoleToFollower(message.getTerm());
				return;
			}
			
			LogEntry lastLogEntry = getLastLogEntry();
			NodeState state = nodeStates.get(node.getId());
			if(message.isSuccess())
			{
				long lastApplied = message.getPrevLogIndex() + message.getEntries();
				if(lastApplied < state.nextIndex) return;
				
				state.nextIndex = lastApplied + 1;
				state.matchIndex = lastApplied;
				
				updateCommitIndex();
				
				if(state.nextIndex <= lastLogEntry.getId())
				{
					logger.debug("{} is not yet up to date, next index is {}", node.getId(), state.nextIndex);
					
					// The node is not yet up to date, send some more data
					sendAppendEntries(node);
				}
			}
			else
			{
				// TODO: We should probably optimize the finding of the next index
				state.nextIndex--;
				logger.debug("Next index for {} is wrong, decrementing to {}", node.getId(), state.nextIndex);
				sendAppendEntries(node);
			}
		}
		finally
		{
			stateLock.unlock();
		}
	}
	
	private void updateCommitIndex()
	{
		long lastCommitIndex = commitIndex;
		int minNodesToMatch = (int) Math.ceil(nodes.size() / 2.0);
		for(long l=commitIndex+1, n=log.last(); l<=n; l++)
		{
			int c = 0;
			for(NodeState state : nodeStates.values())
			{
				if(state.matchIndex >= l)
				{
					if(++c >= minNodesToMatch)
					{
						commitIndex = l;
						break;
					}
				}
			}
		}
		
		if(lastCommitIndex != commitIndex)
		{
			commitUpTo(commitIndex);
		}
	}
	
	private void commitUpTo(long index)
	{
		commitIndex = index;
		
		// Commit all items from lastApplied up to and including index
		logger.debug("Committing up to {}", index);
		
		for(long l=lastApplied+1; l<=index; l++)
		{
			if(futures.containsKey(l))
			{
				futures.get(l).complete(null);
			}
			
			lastApplied = l;
			logger.debug("Applying {}", l);
		}
	}
	
	private void switchRoleToFollower(long term)
	{
		logger.debug("Switching role to follower in term {}", term);
		
		// Schedule that we should start an election if we do not receive a heartbeat soon
		scheduleHearbeatElection();
		
		// Update the state
		stateStorage.updateCurrentTerm(term);
		role = Role.FOLLOWER;
	}
	
	private LogEntry getLastLogEntry()
	{
		if(log.last() == 0) return LOG_HEAD;
		
		try
		{
			return log.get(log.last());
		}
		catch(IOException e)
		{
			throw new RaftException("Unable to load last log entry; " + e.getMessage(), e);
		}
	}
	
	private LogEntry getLogEntry(long id)
	{
		if(id == 0) return LOG_HEAD;
		if(id > log.last()) return null;
		
		try
		{
			return log.get(id);
		}
		catch(IOException e)
		{
			throw new RaftException("Unable to load " + id + "from log; " + e.getMessage(), e);
		}
	}
	
	/**
	 * Send our heartbeat to all of the nodes.
	 */
	private void sendHeartbeat()
	{
		stateLock.lock();
		try
		{
			if(role != Role.LEADER)
			{
				heartbeat.cancel(false);
				heartbeat = null;
				return;
			}
			
			long term = stateStorage.getCurrentTerm();
			LogEntry lastLogEntry = getLastLogEntry();
			for(Node<RaftMessage> node : nodes.values())
			{
				if(node == self) continue;
				
				sendAppendEntries(node, term, lastLogEntry);
			}
		}
		catch(IOException e)
		{
			throw new RaftException("Could not load items from log; " + e.getMessage(), e);
		}
		finally
		{
			stateLock.unlock();
		}
	}
	
	private void sendAppendEntries(Node<RaftMessage> node)
	{
		stateLock.lock();
		try
		{
			if(role != Role.LEADER)
			{
				return;
			}
			
			long term = stateStorage.getCurrentTerm();
			LogEntry lastLogEntry = getLastLogEntry();
			sendAppendEntries(node, term, lastLogEntry);
		}
		catch(IOException e)
		{
			throw new RaftException("Could not load items from log; " + e.getMessage(), e);
		}
		finally
		{
			stateLock.unlock();
		}
	}

	private void sendAppendEntries(Node<RaftMessage> node, long term, LogEntry lastLogEntry)
		throws IOException
	{
		NodeState state = nodeStates.get(node.getId());
		if(state.nextIndex == lastLogEntry.getId() + 1)
		{
			sendTo(node, new AppendEntries(id, term, lastLogEntry.getId(), lastLogEntry.getTerm(), Collections.emptyList(), commitIndex));
		}
		else
		{
			LogEntry entry = getLogEntry(state.nextIndex - 1);
			
			// Send a maximum of five entries at a time
			// TODO: We should really look at the size of the entries instead of just counting
			long[] entriesToSend = new long[Math.min(5, (int) (lastLogEntry.getId() - entry.getId()))];
			for(int i=0, n=entriesToSend.length; i<n; i++)
			{
				entriesToSend[i] = entry.getId() + 1;
			}
			List<LogEntry> entries = log.get(entriesToSend);
			sendTo(node, new AppendEntries(id, term, entry.getId(), entry.getTerm(), entries, commitIndex));
		}
	}
	
	/**
	 * Start a new election.
	 * 
	 */
	private void startElection()
	{
		stateLock.lock();
		try
		{
			role = Role.CANDIDATE;
			long term = stateStorage.getCurrentTerm() + 1;
			
			logger.debug("Starting a new election for term {}", term);
			
			stateStorage.updateCurrentTerm(term);
			stateStorage.updateVote(term, id);
			
			votes.clear();
			votes.add(id);
			
			sendToAll(new RequestVote(term, id, 0, 0));
			
			scheduleElectionTimeout();
		}
		finally
		{
			stateLock.unlock();
		}
	}
	
	/**
	 * Cancel our current election timeout.
	 * 
	 * @return
	 */
	private boolean cancelElectionTimeout()
	{
		if(electionTimeout != null)
		{
			boolean canceled = electionTimeout.cancel(false);
			electionTimeout = null;
			return canceled;
		}
		
		return true;
	}
	
	/**
	 * Schedule a new election timeout.
	 * 
	 */
	private void scheduleElectionTimeout()
	{
		logger.debug("Scheduling an election due to missing heartbeat");
		cancelElectionTimeout();
		electionTimeout = scheduler.schedule(this::startElection, 150 + random.nextInt(150), TimeUnit.MILLISECONDS);
	}
	
	/**
	 * Schedule an election if we do not receive a heartbeat within a certain period.
	 */
	private void scheduleHearbeatElection()
	{
		cancelElectionTimeout();
		electionTimeout = scheduler.schedule(this::scheduleElectionTimeout, 10, TimeUnit.SECONDS);
	}

	/**
	 * Schedule that we should send a heartbeat with a certain interval.
	 * 
	 */
	private void scheduleSendHeartbeat()
	{
		cancelElectionTimeout();
		heartbeat = scheduler.scheduleAtFixedRate(this::sendHeartbeat, 5, 5, TimeUnit.SECONDS);
	}
	
	/**
	 * Send a message to all of the nodes (except self).
	 * 
	 * @param msg
	 */
	private void sendToAll(RaftMessage msg)
	{
		for(Node<RaftMessage> n : nodes.values())
		{
			if(n != self)
			{
				n.getChannel().send(msg);
			}
		}
	}
	
	/**
	 * Send a message to a specific node.
	 * 
	 * @param node
	 * @param msg
	 */
	private void sendTo(Node<RaftMessage> node, RaftMessage msg)
	{
		node.getChannel().send(msg);
	}

	enum Role
	{
		FOLLOWER,
		
		CANDIDATE,
		
		LEADER
	}
	
	private static class NodeState
	{
		private long nextIndex;
		private long matchIndex;
		
		public NodeState(long nextIndex)
		{
			this.nextIndex = nextIndex;
		}
	}
}