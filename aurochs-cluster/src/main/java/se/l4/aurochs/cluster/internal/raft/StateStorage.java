package se.l4.aurochs.cluster.internal.raft;

public interface StateStorage
{
	/**
	 * Get the current election term.
	 * 
	 * @return
	 */
	long getCurrentTerm();
	
	/**
	 * Update the current election term.
	 * 
	 * @param term
	 */
	void updateCurrentTerm(long term);
	
	/**
	 * Get who we voted for in the given term.
	 * 
	 * @param term
	 * @return
	 */
	String getVote(long term);
	
	/**
	 * Set who we voted for in the given term.
	 * 
	 * @param term
	 *   the term
	 * @param id
	 *   id of node voted for, or {@code null} if no vote cast
	 */
	void updateVote(long term, String id);
	
	/**
	 * Remove all of our votes.
	 * 
	 */
	void clearVotes();
	
	/**
	 * Get the latest log index that has been applied.
	 * 
	 * @return
	 */
	long getApplyIndex();
	
	/**
	 * Update the latest log index that has been applied.
	 * 
	 * @param index
	 */
	void updateApplyIndex(long index);
}
