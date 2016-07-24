package werewolf.game;

import java.util.logging.Logger;

import werewolf.net.ForumMessage;
import werewolf.net.ForumPost;
import werewolf.net.ForumUser;

public class Player extends ForumUser
{
	private final static Logger	LOGGER				= Logger.getLogger(Player.class.getName());
	private static final long	serialVersionUID	= 4363493461063986117L;

	private boolean				alive				= true;
	private boolean				modkilled			= false;
	private int					injured				= 0;
	private ForumMessage		data				= new ForumMessage();
	private ForumPost			joinPost;
	private WerewolfGame		game;

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
	 * @return A log of the events that have happened to this player.
	 */
	public ForumMessage getData()
	{
		return this.data;
	}

	/**
	 * @return The post where this player joined or was added to the game.
	 */
	public ForumPost getJoinPost()
	{
		return this.joinPost;
	}

	/**
	 * @return A forum message formatted to reflect the player's current status
	 *         (alive / dead / injured).
	 */
	public ForumMessage getMessageName()
	{
		ForumMessage name = new ForumMessage();
		if (!this.alive)
			name.startStrike().startColor(ForumMessage.DEAD);
		else if (this.injured > 0)
			name.startColor(ForumMessage.DEAD);
		name.add(this.getName()).stopColor().stopStrike();
		return name;
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

	public boolean isModkilled()
	{
		return this.modkilled;
	}

	/**
	 * Marks this player as dead.
	 *
	 * @param evt
	 *            The event surrounding this player's death.
	 */
	public void kill(ForumMessage evt, int round, ForumPost origin)
	{
		this.logEvent(evt, round, origin);
		this.alive = false;
	}

	/**
	 * @param evt
	 *            The event this player was involved in.
	 * @param origin
	 *            The original post which caused or documented this event.
	 */
	public void logEvent(ForumMessage evt, ForumPost origin)
	{
		evt = new ForumMessage().startURL(origin.getUrl()).add(evt).stopURL();
		if (this.data.hasTextSegments())
			this.data.add(", ");
		this.data.add(evt);
	}

	/**
	 * @param evt
	 *            The event this player was involved in.
	 * @param round
	 *            The round this event happened.
	 * @param origin
	 *            The original post which caused or documented this event.
	 */
	public void logEvent(ForumMessage evt, int round, ForumPost origin)
	{
		evt.add(" R" + round);
		this.logEvent(evt, origin);
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
	public void revive(ForumMessage evt, int round, ForumPost origin)
	{
		if (this.alive)
			throw new IllegalArgumentException("Player already alive.");
		this.logEvent(evt, round, origin);
		this.alive = true;
	}

	public void setAlive(boolean status)
	{
		this.alive = status;
	}

	public void setModkilled(boolean modkilled)
	{
		this.modkilled = modkilled;
	}
}
