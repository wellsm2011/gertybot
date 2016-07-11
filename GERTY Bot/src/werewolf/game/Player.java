package werewolf.game;

import java.util.logging.Logger;

import werewolf.net.ForumPost;
import werewolf.net.ForumUser;
import werewolf.net.msg.ForumMessageColor;
import werewolf.net.msg.ForumMessageContainer;
import werewolf.net.msg.ForumMessageElement;
import werewolf.net.msg.ForumMessageStrike;
import werewolf.net.msg.ForumMessageString;
import werewolf.net.msg.ForumMessageUrl;

public class Player extends ForumUser
{
	private final static Logger		LOGGER				= Logger.getLogger(Player.class.getName());

	private static final long		serialVersionUID	= 4363493461063986117L;

	private boolean					alive				= true;
	private int						injured				= 0;
	private ForumMessageContainer	data				= new ForumMessageContainer();
	private ForumPost				joinPost;
	private WerewolfGame			game;

	/**
	 * Creates a new player with the given forum user and join post.
	 *
	 * @param player
	 *            The forum user to represent this player.
	 * @param joinPost
	 *            The post where the forum user either joined or was added to
	 *            the game.
	 */
	public Player(ForumUser player, ForumPost joinPost, WerewolfGame game)
	{
		super(player);
		this.joinPost = joinPost;
		Player.LOGGER.fine("Player created: " + player);
		this.game = game;
	}

	/**
	 * @param evt
	 *            The event this player was involved in.
	 * @param round
	 *            The round this event happened.
	 * @param origin
	 *            The original post which caused or documented this event.
	 */
	public void logEvent(ForumMessageElement evt, int round, ForumPost origin)
	{
		evt.append(" R" + round);
		logEvent(evt, origin);
	}

	/**
	 * @param evt
	 *            The event this player was involved in.
	 * @param origin
	 *            The original post which caused or documented this event.
	 */
	public void logEvent(ForumMessageElement evt, ForumPost origin)
	{
		evt = new ForumMessageUrl(origin.getUrl(), evt);
		if (!this.data.getChildren().isEmpty())
			this.data.append(", ");
		this.data.append(evt);
	}

	/**
	 * @return A log of the events that have happened to this player.
	 */
	public ForumMessageElement getData()
	{
		return this.data;
	}

	/**
	 * @return A forum message formatted to reflect the player's current status
	 *         (alive / dead / injured).
	 */
	public ForumMessageElement getMessageName()
	{
		ForumMessageElement name = new ForumMessageString(getName());
		if (!this.alive)
			name = new ForumMessageStrike(new ForumMessageColor(ForumMessageColor.DEAD, name));
		else if (this.injured > 0)
			name = new ForumMessageColor(ForumMessageColor.DEAD, name);
		return name;
	}

	/**
	 * @return The post where this player joined or was added to the game.
	 */
	public ForumPost getJoinPost()
	{
		return this.joinPost;
	}

	/**
	 * Marks this player as being injured for one round.
	 */
	public void injure()
	{
		this.injure(1);
	}

	/**
	 * Marks this player as being injured for the specified number of rounds.
	 */
	public void injure(int rounds)
	{
		this.injured = Math.max(1, this.injured) + rounds;
	}

	/**
	 * @return True if the player is currently alive.
	 */
	public boolean isAlive()
	{
		return this.alive;
	}

	/**
	 * @return True if the player is currently injured, false otherwise.
	 */
	public boolean isInjured()
	{
		return this.injured > 0;
	}

	/**
	 * Marks this player as dead.
	 *
	 * @param evt
	 *            The event surrounding this player's death.
	 */
	public void kill(ForumMessageElement evt, int round, ForumPost origin)
	{
		this.logEvent(evt, round, origin);
		this.alive = false;
	}

	/**
	 * Marks this player as replacing the given player, and copies the old
	 * player's data and status to this player.
	 *
	 * @param plr
	 *            The player that this player is a replacement for.
	 */
	public void replacePlayer(Player plr)
	{
		Player.LOGGER.fine("Player replacement: " + this + " for " + plr);
		this.data = plr.data;
		this.alive = plr.alive;
		this.injured = plr.injured;
		this.game = plr.game;
	}

	/**
	 * Marks this player as alive again.
	 *
	 * @param evt
	 *            The event surrounding this player's death.
	 * @return False if the player was already alive, true otherwise.
	 */
	public void revive(ForumMessageElement evt, int round, ForumPost origin)
	{
		if (this.alive)
			throw new IllegalArgumentException("Player already alive.");
		this.logEvent(evt, round, origin);
		this.alive = true;
	}
}
