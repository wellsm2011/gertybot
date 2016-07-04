package werewolf.game;

import java.util.LinkedList;
import java.util.logging.Logger;

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

	public boolean isHost(ForumUser user)
	{
		return host.equals(user) || cohost.equals(user) || isMe(user);
	}

	public Player getPlayer(ForumUser user)
	{
		for (Player player : players)
		{
			if (player.equals(user))
				return player;
		}
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
		return thread.getContext().getUserDatabase().getUserFromExternalSource(name, this.players);
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
		return thread.getContext().getUserDatabase().getUserFromExternalSource(name);
	}

	/**
	 * Returns the current phase of the game.
	 * 
	 * @return
	 */
	public GamePhase getPhase()
	{
		return phase;
	}

	/**
	 * Returns the next phase in the cycle.
	 * 
	 * @return
	 */
	protected GamePhase nextPhase()
	{
		// Pregame -> Night -> Day -> Night ...
		if (phase.equals(GamePhase.NIGHT))
			return GamePhase.DAY;
		return GamePhase.NIGHT;
	}

	public boolean allowedPhase(GamePhase phase)
	{
		return phase.equals(GamePhase.NIGHT) || phase.equals(GamePhase.DAY);
	}

	/**
	 * Advances the game to the next phase.
	 */
	public void advance()
	{
		advance(nextPhase());
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
		if (!allowedPhase(advance))
			return;
		do
		{
			phase.resolve(this);
			phase = nextPhase();
		} while (!phase.equals(advance));
	}

	public ForumContext getContext()
	{
		return thread.getContext();
	}

	public LinkedList<Player> getPlayers()
	{
		return players;
	}
}
