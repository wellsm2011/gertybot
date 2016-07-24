package werewolf.game;

import java.io.Serializable;
import java.util.function.Supplier;
import java.util.logging.Logger;

import werewolf.net.ForumPost;

public class Vote implements Serializable
{
	private final static Logger	LOGGER				= Logger.getLogger(Vote.class.getName());
	private static final long	serialVersionUID	= -479947774406039167L;

	private Player				voter;
	private User				target;
	private ForumPost			post;
	private boolean				hidden;
	private boolean				valid				= true;

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
		this(voter, target, null, false);
		Vote.LOGGER.fine("Vote created: " + this);
	}

	/**
	 * Creates a new vote that is not associated with a forum post.
	 *
	 * @param voter
	 *            The voting user.
	 * @param target
	 *            The target user.
	 * @param hidden
	 *            True if the vote is 'hidden' from the tally.
	 */
	public Vote(Player voter, User target, boolean hidden)
	{
		this(voter, target, null, hidden);
	}

	/**
	 * Creates a new vote that is associated with a forum post.
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
		this(voter, target, post, false);
	}

	/**
	 * Creates a new vote that is associated with a forum post.
	 *
	 * @param voter
	 *            The voting user.
	 * @param target
	 *            The target user.
	 * @param post
	 *            The post where the vote was cast.
	 * @param hidden
	 *            True if the vote is 'hidden' from the tally.
	 */
	public Vote(Player voter, User target, ForumPost post, boolean hidden)
	{
		this.voter = voter;
		this.target = target;
		this.post = post;
		this.hidden = hidden;
	}

	/**
	 * @return The post that caused this vote.
	 */
	public ForumPost getPost()
	{
		return this.post;
	}

	/**
	 * @return The target user who was voted.
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

	/**
	 * @return True if this vote is intended to be hidden from the publicly
	 *         posted tally.
	 */
	public boolean isHidden()
	{
		return this.hidden;
	}

	@Override
	public String toString()
	{
		return this.voter + " -> " + this.target;
	}
}
