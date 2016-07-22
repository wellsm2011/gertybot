package werewolf.game.cmd;

import java.util.function.BiFunction;

import werewolf.game.GamePhase;
import werewolf.game.Player;
import werewolf.game.WerewolfGame;
import werewolf.net.Command;
import werewolf.net.ForumUser;

public abstract class GameCommand
{
	protected static class InvalidatonException extends Exception
	{
		public InvalidatonException(String msg)
		{
			super(msg);
		}
	}

	protected enum Requirement
	{
		HOST("invalid access", "can't be host"), PLAYER("unknown player", "can't be a player"), ALIVE("dead player", "living player"), DAY("must be day", "currently day"), NIGHT("must be night",
				"currently night"), PREGAME("must be pregame setup", "currently pregame setup"), ADMIN("invalid access", "can't be admin");

		public final String									requiredTrue;
		public final String									requiredFalse;
		private BiFunction<WerewolfGame, Command, Boolean>	resolver	= null;

		private Requirement(String requiredTrue, String requiredFalse)
		{
			this.requiredTrue = requiredTrue;
			this.requiredFalse = requiredFalse;
		}

		public void assertRequirement(boolean expectedResult, WerewolfGame game, Command cmd)
		{
			boolean state = this.resolver.apply(game, cmd);
			if (state && !expectedResult)
				cmd.invalidate(this.requiredFalse);
			else if (!state && expectedResult)
				cmd.invalidate(this.requiredTrue);
			assert state == expectedResult;
		}

		protected void setResolver(BiFunction<WerewolfGame, Command, Boolean> resolver)
		{
			if (this.resolver == null)
				this.resolver = resolver;
			else
				throw new IllegalArgumentException("Resolver already set for " + this.name());
		}
	}

	static
	{
		Requirement.HOST.setResolver((WerewolfGame game, Command cmd) -> {
			return game.isHost(cmd.getUser());
		});
		Requirement.PLAYER.setResolver((WerewolfGame game, Command cmd) -> {
			return game.getPlayer(cmd.getUser()) != null;
		});
		Requirement.ALIVE.setResolver((WerewolfGame game, Command cmd) -> {
			// Assert that the user of the command is a player.
				Requirement.PLAYER.assertRequirement(true, game, cmd);
				return game.getPlayer(cmd.getUser()).isAlive();
			});
		Requirement.DAY.setResolver((WerewolfGame game, Command cmd) -> {
			return game.getPhase().equals(GamePhase.DAY);
		});
		Requirement.NIGHT.setResolver((WerewolfGame game, Command cmd) -> {
			return game.getPhase().equals(GamePhase.NIGHT);
		});
		Requirement.PREGAME.setResolver((WerewolfGame game, Command cmd) -> {
			return game.getPhase().equals(GamePhase.PREGAME);
		});
		Requirement.ADMIN.setResolver((WerewolfGame game, Command cmd) -> {
			return game.isHost(cmd.getUser());
		});
	}

	// Usage: player[:<alive|dead>], string[:<option1>|<option2>|<...>],
	// number[:<min>,<max>]
	protected String									name		= "UNKNOWN";
	protected String									info		= "No information found for command.";
	protected String									usage		= "";
	protected String									match		= ".*";
	protected Requirement[]								mustBeTrue	= new Requirement[0];
	protected Requirement[]								mustBeFalse	= new Requirement[0];
	protected WerewolfGame								game;

	public GameCommand(WerewolfGame game)
	{
		this.game = game;
	}

	/**
	 * Executes this command. Should only be used internally by the base class
	 * during processCmd().
	 * 
	 * @param cmd
	 *            The user-supplied command that triggered this execution.
	 * @return
	 * @throws InvalidatonException
	 *             If any of this command's assertions fail.
	 * @throws IndexOutOfBoundsException
	 *             If this command requires additional command parameters which
	 *             were not supplied.
	 */
	protected abstract boolean execute(Command cmd) throws InvalidatonException, IndexOutOfBoundsException;

	/**
	 * @return Returns the description of this command, to be posted in the help
	 *         section.
	 */
	public ForumMessageElement getInfo()
	{
		return this.info;
	}

	/**
	 * @return The name of this command.
	 */
	public String getName()
	{
		return this.name;
	}

	/**
	 * Returns a ForumUser specified by the given string.
	 * 
	 * @param user
	 * @return
	 * @throws InvalidatonException
	 *             If no user could be found.
	 */
	protected ForumUser getUser(String user) throws InvalidatonException
	{
		ForumUser usr = game.getUser(user);
		if (usr == null)
			throw new InvalidatonException("user required: " + user);
		return usr;
	}

	/**
	 * Returns a ForumUser, specified by the given string, who is not in the
	 * game.
	 * 
	 * @param user
	 * @return
	 * @throws InvalidatonException
	 *             If no user could be found, or if the given user is already a
	 *             player.
	 */
	protected ForumUser getNonPlayer(String user) throws InvalidatonException
	{
		ForumUser usr = game.getUser(user);
		if (usr == null || game.getPlayer(usr) != null)
			throw new InvalidatonException("non-player required: " + user);
		return usr;
	}

	/**
	 * Returns a player represented by the given String.
	 * 
	 * @param player
	 * @return
	 * @throws InvalidatonException
	 */
	protected Player getPlayer(String player) throws InvalidatonException
	{
		Player plr = game.getPlayer(player);
		if (plr == null)
			throw new InvalidatonException("player required: " + player);
		return plr;
	}

	/**
	 * Returns a player represented by the given ForumUser.
	 * 
	 * @param player
	 * @return
	 * @throws InvalidatonException
	 *             If a player could not be found.
	 */
	protected Player getPlayer(ForumUser player) throws InvalidatonException
	{
		Player plr = game.getPlayer(player);
		if (plr == null)
			throw new InvalidatonException("living player required: " + player);
		return plr;
	}

	/**
	 * Returns a dead player represented by the given string.
	 * 
	 * @param player
	 * @return
	 * @throws InvalidatonException
	 *             If a player could not be found, or if the selected player is
	 *             alive.
	 */
	protected Player getDeadPlayer(String player) throws InvalidatonException
	{
		Player plr = game.getPlayer(player);
		if (plr == null || plr.isAlive())
			throw new InvalidatonException("dead player required: " + player);
		return plr;
	}

	/**
	 * Returns a living player represented by the given string.
	 * 
	 * @param player
	 * @return
	 * @throws InvalidatonException
	 *             If a player could not be found, or if the selected player is
	 *             dead.
	 */
	protected Player getLivingPlayer(String player) throws InvalidatonException
	{
		Player plr = game.getPlayer(player);
		if (plr == null || plr.isAlive())
			throw new InvalidatonException("living player required: " + player);
		return plr;
	}

	/**
	 * Returns the integer represented by the given string.
	 * 
	 * @param integer
	 * @return
	 * @throws InvalidatonException
	 *             if the string cannot be parsed.
	 */
	protected Integer getInteger(String integer) throws InvalidatonException
	{
		try
		{
			return Integer.parseInt(integer);
		} catch (NumberFormatException ex)
		{
			throw new InvalidatonException("number required: " + integer);
		}
	}

	/**
	 * Verifies requirements for the state of the game, the user making the
	 * command, etc.
	 * 
	 * @param cmd
	 * @return
	 */
	protected boolean isValid(Command cmd)
	{
		try
		{
			for (Requirement req : this.mustBeTrue)
				req.assertRequirement(true, this.game, cmd);
			for (Requirement req : this.mustBeFalse)
				req.assertRequirement(false, this.game, cmd);
			return true;
		} catch (AssertionError ex)
		{
			return false;
		}
	}

	/**
	 * Processes a new command. Performs checks to ensure that this command can
	 * be processed by this GameCommand.
	 * 
	 * @param game
	 * @param cmd
	 * @return true if the bot needs to make a new post.
	 */
	public boolean processCmd(Command cmd)
	{
		// Only process valid commands.
		if (cmd.isInvalidated())
			return false;
		if (!cmd.getCommand().toLowerCase().matches("^(" + this.match + ")$"))
			return false;
		if (!this.isValid(cmd))
			return false;
		try
		{
			return this.execute(cmd);
		} catch (InvalidatonException ex)
		{
			cmd.invalidate(ex.getMessage());
		} catch (IndexOutOfBoundsException ex)
		{
			cmd.invalidate("not enough parameters");
		}
		return false;
	}
}
