package werewolf.game;

import java.util.LinkedList;
import java.util.logging.Logger;

import werewolf.net.ForumPost;
import werewolf.net.ForumUser;

public class Player extends ForumUser
{
	private final static Logger		LOGGER				= Logger.getLogger(Player.class.getName());

	private static final long		serialVersionUID	= 4363493461063986117L;

	private boolean					alive				= true;
	private int						injured				= 0;
	private LinkedList<PlayerEvent>	data				= new LinkedList<PlayerEvent>();
	private PlayerEvent				lastDeath			= null;
	private ForumPost				joinPost;
	private WerewolfGame game;

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
	 */
	public void addData(PlayerEvent evt)
	{
		this.data.add(evt);
	}

	/**
	 * @return A plaintext log of the events that have happened to this player.
	 */
	public String getData()
	{
		if (this.data.isEmpty())
			return "";

		StringBuilder output = new StringBuilder();
		output.append(" - ");
		boolean isFirst = true;
		for (PlayerEvent event : this.data)
		{
			if (!isFirst)
				output.append(", ");
			output.append(event.getData());
			isFirst = false;
		}
		return output.toString();
	}

	/**
	 * @return A BBCode formatted log of the events that have happened to this
	 *         player.
	 */
	public String getForumData()
	{
		if (this.data.isEmpty())
			return "";

		StringBuilder output = new StringBuilder();
		output.append(" - ");
		boolean isFirst = true;
		for (PlayerEvent event : this.data)
		{
			if (!isFirst)
				output.append(", ");
			output.append(event.makeLink(event.getData()));
			isFirst = false;
		}
		return output.toString();
	}

	/**
	 * @return The post where this player joined or was added to the game.
	 */
	public ForumPost getJoinPost()
	{
		return this.joinPost;
	}

	/**
	 * @return A shortened link to the last death of the player, or null if the
	 *         player has not died yet.
	 */
	public String getLastDeath()
	{
		if (this.lastDeath == null)
			return "";
		return this.lastDeath.makeLink(this.lastDeath.getRound());
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
	public void kill(PlayerEvent evt)
	{
		this.addData(evt);
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
		this.lastDeath = plr.lastDeath;
		this.game = plr.game;
	}

	/**
	 * Marks this player as alive again.
	 *
	 * @param evt
	 *            The event surrounding this player's death.
	 * @return False if the player was already alive, true otherwise.
	 */
	public void revive(PlayerEvent evt)
	{
		if (this.alive)
			throw new IllegalArgumentException("Player already alive.");
		this.addData(evt);
		this.alive = true;
	}
}
