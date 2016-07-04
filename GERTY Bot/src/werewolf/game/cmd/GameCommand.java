package werewolf.game.cmd;

import java.util.List;
import java.util.function.BiFunction;

import werewolf.game.GamePhase;
import werewolf.game.Player;
import werewolf.game.WerewolfGame;
import werewolf.net.Command;

public abstract class GameCommand
{
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

		protected void setResolver(BiFunction<WerewolfGame, Command, Boolean> resolver)
		{
			if (this.resolver == null)
				this.resolver = resolver;
			else
				throw new IllegalArgumentException("Resolver already set for " + this.name());
		}

		public void assertRequirement(boolean expectedResult, WerewolfGame game, Command cmd)
		{
			boolean state = resolver.apply(game, cmd);
			if (state && !expectedResult)
				cmd.invalidate(this.requiredFalse);
			else if (!state && expectedResult)
				cmd.invalidate(this.requiredTrue);
			assert state == expectedResult;
		}
	}

	// Usage: player[:<alive|dead>], string[:<option1>|<option2>|<...>],
	// number[:<min>,<max>]
	protected String		name		= "UNKNOWN";
	protected String		info		= "No information found for command.";
	protected String		match		= ".*";
	protected Requirement[]	mustBeTrue	= new Requirement[0];
	protected Requirement[]	mustBeFalse	= new Requirement[0];
	protected WerewolfGame	game;

	public GameCommand(WerewolfGame game)
	{
		this.game = game;
	}

	public String getName()
	{
		return name;
	}

	/**
	 * @param game
	 * @param cmd
	 * @return true if the bot needs to make a new post.
	 */
	public boolean processCmd(Command cmd)
	{
		if (!cmd.getCommand().matches("^(" + match + ")$"))
			return false;
		if (!isValid(cmd))
			return false;
		return execute(cmd);
	}

	protected boolean isValid(Command cmd)
	{
		try
		{
			for (Requirement req : mustBeTrue)
				req.assertRequirement(true, game, cmd);
			for (Requirement req : mustBeFalse)
				req.assertRequirement(false, game, cmd);

			// TODO: Evaluate Sig here.

			return true;
		} catch (AssertionError ex)
		{
			return false;
		}
	}

	protected abstract boolean execute(Command cmd);
}
