package se.l4.aurochs.cluster.internal.raft;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;
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

import se.l4.aurochs.cluster.internal.raft.log.Log;
import se.l4.aurochs.cluster.internal.raft.log.LogEntry;
import se.l4.aurochs.cluster.internal.raft.messages.AppendEntries;
import se.l4.aurochs.cluster.internal.raft.messages.AppendEntriesReply;
import se.l4.aurochs.cluster.internal.raft.messages.RaftMessage;
import se.l4.aurochs.cluster.internal.raft.messages.RequestVote;
import se.l4.aurochs.cluster.internal.raft.messages.RequestVoteReply;
import se.l4.aurochs.core.channel.Channel;
import se.l4.aurochs.core.channel.ChannelListener;
import se.l4.aurochs.core.channel.MessageEvent;
import se.l4.crayon.services.ManagedService;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class Raft
	implements ManagedService
{
	private static final long LOG_CACHE_SIZE = 10;

	private final Logger logger;
	
	private final StateStorage stateStorage;
	private final Log log;
	
	private final ScheduledExecutorService scheduler;
	private final ExecutorService executor;
	
	private final ChannelListener<RaftMessage> channelListener;
	
	private final Random random;
	private final Lock stateLock;
	
	private final String id;
	
	private final LinkedList<LogEntry> logCache;
	
	private ScheduledFuture<?> electionTimeout;
	private ScheduledFuture<?> heartbeat;
	
	private Role role;
	
	private Map<String, Node<RaftMessage>> nodes;
	private Node<RaftMessage> self;

	private Set<String> votes;

	private Node<RaftMessage> leader;
	
	
	public Raft(StateStorage stateStorage, Log log, Nodes nodes, String id)
	{
		this.logger = LoggerFactory.getLogger(Raft.class.getName() + "[" + id + "]");
		
		this.stateStorage = stateStorage;
		this.log = log;
		
		this.id = id;
		
		ThreadFactory threadFactory = new ThreadFactoryBuilder()
			.setNameFormat("Raft[" + id + "] %s")
			.setUncaughtExceptionHandler(this::uncaughtException)
			.build();
		scheduler = Executors.newScheduledThreadPool(1, threadFactory);
		executor = Executors.newCachedThreadPool(threadFactory);
		
		stateLock = new ReentrantLock();
		random = new Random();
		
		logCache = createLogCache(log);
		
		this.channelListener = createChannelListener();
		this.nodes = Maps.newHashMap();
		
		nodes.listen(this::handleNodeEvent);
		
		votes = Sets.newHashSet();
		
		if(self == null)
		{
			throw new IllegalArgumentException("Nodes did not include self with id " + id);
		}
		
		role = Role.FOLLOWER;
		scheduleElectionTimeout();
	}
	
	private void uncaughtException(Thread t, Throwable e)
	{
		logger.warn("Exception caught in background; " + e.getMessage(), e);
	}
	
	
	private LinkedList<LogEntry> createLogCache(Log log)
	{
		try
		{
			LinkedList<LogEntry> result = new LinkedList<>();
			long to = log.last();
			long from = Math.max(to - LOG_CACHE_SIZE, 1);
			for(long l=from; l<=to; l++)
			{
				result.add(log.get(l));
			}
			return result;
		}
		catch(IOException e)
		{
			throw new RaftException("Unable to create initial cache, could not load from log; " + e.getMessage(), e);
		}
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
			}
		};
	}
	
	private void handleNodeEvent(NodeEvent<Object> event)
	{
		Node<Object> node = event.getNode();
		
		// TODO: Support transforming data in the channel
		Channel<RaftMessage> channel = node.getChannel()
			.filter(RaftMessage.class)
			.on(executor);
		
		channel.addListener(channelListener);
		
		Node<RaftMessage> transformed = new Node<>(node.getId(), channel);
		
		if(this.id.equals(node.getId()))
		{
			self = transformed;
		}
		
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
			if(vote == null || vote.equals(node.getId()))
			{
				// TODO: Check log here
				logger.debug("First vote, voting yes to node {}", node.getId());
				stateStorage.updateVote(message.getTerm(), node.getId());
				sendTo(node, new RequestVoteReply(id, newTerm, true));
			}
			else
			{
				logger.debug("Already voted for {} instead of {}, voting no", vote, node.getId());
				sendTo(node, new RequestVoteReply(id, newTerm, false));
			}
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
			
			if(reply.isVoteGranted() && role != Role.LEADER)
			{
				logger.debug("Got a reply from {} who voted yes, currently {} votes yes", node.getId(), votes.size() + 1);
				
				votes.add(node.getId());
				if(isMajority(votes.size(), nodes.size()))
				{
					cancelElectionTimeout();
					
					role = Role.LEADER;
					leader = self;
					
					logger.debug("Became leader, due to having " + votes.size() + " votes of " + nodes.size() + " total");
					
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
	
	protected void handleAppendEntries(Node<RaftMessage> node, AppendEntries message)
	{
		stateLock.lock();
		try
		{
			logger.debug("Got append entries from {} when in role {}", node.getId(), role);
			
			long term = stateStorage.getCurrentTerm();
			if(term < message.getTerm())
			{
				logger.debug("Making {} leader instead of self, instead becoming follower", node.getId());
				
				switchRoleToFollower(message.getTerm());
				
				// And update our local leader
				leader = node;
			}
			else if(term > message.getTerm())
			{
				scheduleHearbeatElection();
				
				sendTo(node, new AppendEntriesReply(id, term, false));
				return;
			}
			
			scheduleHearbeatElection();
		}
		finally
		{
			stateLock.unlock();
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
			
			sendToAll(
				new AppendEntries(id, stateStorage.getCurrentTerm(), 0, 0, null, 0)
			);
		}
		finally
		{
			stateLock.unlock();
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
}
