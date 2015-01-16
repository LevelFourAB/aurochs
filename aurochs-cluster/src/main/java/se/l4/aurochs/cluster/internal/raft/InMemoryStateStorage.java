package se.l4.aurochs.cluster.internal.raft;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@link StateStorage} that stores the state in memory, useful for testing.
 * 
 * @author Andreas Holstenson
 *
 */
public class InMemoryStateStorage
	implements StateStorage
{
	private long currentTerm;
	private Map<Long, String> votes;
	private long applyIndex;
	
	public InMemoryStateStorage()
	{
		votes = new HashMap<>();
	}

	@Override
	public long getCurrentTerm()
	{
		return currentTerm;
	}

	@Override
	public void updateCurrentTerm(long term)
	{
		this.currentTerm = term;
	}

	@Override
	public String getVote(long term)
	{
		return votes.get(term);
	}

	@Override
	public void updateVote(long term, String id)
	{
		votes.put(term, id);
	}

	@Override
	public void clearVotes()
	{
		votes.clear();
	}

	@Override
	public long getApplyIndex()
	{
		return applyIndex;
	}
	
	@Override
	public void updateApplyIndex(long index)
	{
		applyIndex = index;
	}
}
