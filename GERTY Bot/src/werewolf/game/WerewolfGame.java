package werewolf.game;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import werewolf.game.cmd.GameCommand;
import werewolf.net.Command;
import werewolf.net.ForumContext;
import werewolf.net.ForumThread;
import werewolf.net.ForumUser;

public class WerewolfGame
{
	// Setup -> R0 Night -> R1 Day
	// Setup -> R1 Day

	private static final Logger	LOGGER	= Logger.getLogger(WerewolfGame.class.getName());

	private static boolean isMe(ForumUser user)
	{
		return user.equals(user.getContext().getLogin());
	}

	private ForumThread			thread;
	private LinkedList<Player>	players		= new LinkedList<Player>();
	private VoteManager			votes		= new VoteManager(this);
	private ForumUser			host		= null;
	private ForumUser			cohost		= null;
	private int					round		= 0;
	// Game starts in pregame setup.
	private GamePhase			phase		= GamePhase.PREGAME;

	private boolean				stateChange	= false;
	private GameCommand[]		cmds		= new GameCommand[]
											{};

	public WerewolfGame(ForumThread subscription)
	{
		this.thread = subscription;
	}

	/**
	 * Advances the game to the next phase.
	 */
	public void advance()
	{
		this.advance(this.nextPhase());
	}

	/**
	 * Continuously advances the phase until the provided phase is the current
	 * phase.
	 * 
	 * @param advance
	 *            The Phase to advance to.
	 */
	public void advance(GamePhase advance)
	{
		if (!this.allowedPhase(advance))
			return;
		do
		{
			this.phase.resolve(this);
			this.phase = this.nextPhase();
		} while (!this.phase.equals(advance));
	}

	public boolean allowedPhase(GamePhase phase)
	{
		return phase.equals(GamePhase.NIGHT) || phase.equals(GamePhase.DAY);
	}

	/**
	 * @return The current list of available commands this game recognizes.
	 */
	public List<GameCommand> getCmds()
	{
		return Arrays.asList(this.cmds);
	}

	public ForumContext getContext()
	{
		return this.thread.getContext();
	}

	/**
	 * Returns the current phase of the game.
	 * 
	 * @return
	 */
	public GamePhase getPhase()
	{
		return this.phase;
	}

	public Player getPlayer(ForumUser user)
	{
		for (Player player : this.players)
			if (player.equals(user))
				return player;
		return null;
	}

	/**
	 * Same as getUser(String), except the search only covers players in this
	 * game.
	 * 
	 * @param name
	 * @return
	 */
	public Player getPlayer(String name)
	{
		return this.thread.getContext().getUserDatabase().getUserFromExternalSource(name, this.players);
	}

	public LinkedList<Player> getPlayers()
	{
		return this.players;
	}

	/**
	 * Queries the UserDatabase of the context of this WerewolfGame for a given
	 * string. Equivalent to
	 * WerewolfGame.getContext().getUserDatabase().getUserFromExternalSource
	 * (name);
	 * 
	 * @param name
	 * @return
	 */
	public ForumUser getUser(String name)
	{
		return this.thread.getContext().getUserDatabase().getUserFromExternalSource(name);
	}

	public boolean isHost(ForumUser user)
	{
		return this.host.equals(user) || this.cohost.equals(user) || WerewolfGame.isMe(user);
	}

	/**
	 * Returns the next phase in the cycle.
	 * 
	 * @return
	 */
	protected GamePhase nextPhase()
	{
		// Pregame -> Night -> Day -> Night ...
		if (this.phase.equals(GamePhase.NIGHT))
			return GamePhase.DAY;
		return GamePhase.NIGHT;
	}

	public void processCmd(Command cmd)
	{
		for (GameCommand gcmd : this.getCmds())
			this.stateChange |= gcmd.processCmd(cmd);
	}
}
