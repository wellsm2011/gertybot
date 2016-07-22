package werewolf.game;

import java.io.Serializable;
import java.util.logging.Logger;

import werewolf.net.ForumPost;

public class Vote implements Serializable
{
	private final static Logger	LOGGER				= Logger.getLogger(Vote.class.getName());
	private static final long	serialVersionUID	= -479947774406039167L;

	private Player			voter;
	private User			target;
	public final ForumPost	post;

	/**
	 * Creates a new vote that is not associated with any forum post.
	 *
	 * @param voter
	 *            The voting user.
	 * @param target
	 *            The target user.
	 */
	public Vote(Player voter, User target)
	{
		this(voter, target, null);
		Vote.LOGGER.fine("Vote created: " + this);
	}

	/**
	 * Creates a new vote that is ascociated with a forum post.
	 *
	 * @param voter
	 *            The voting user.
	 * @param target
	 *            The target user.
	 * @param post
	 *            The post where the vote was cast.
	 */
	public Vote(Player voter, User target, ForumPost post)
	{
		this.voter = voter;
		this.target = target;
		this.post = post;
	}

	/**
	 * @return The target user.
	 */
	public User getTarget()
	{
		return this.target;
	}

	/**
	 * @return A url link to the post where the vote was cast, or an empty
	 *         string if no post is ascociated with this vote.
	 */
	public String getVoteLink()
	{
		if (this.post != null && this.post.getPostId() > 0)
			return this.post.getUrl();
		return "";
	}

	/**
	 * @return The voting user.
	 */
	public Player getVoter()
	{
		return this.voter;
	}

	@Override
	public String toString()
	{
		return this.voter + " -> " + this.target;
	}
}
