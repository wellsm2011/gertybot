package werewolf.game.cmd;

import java.util.List;

import werewolf.game.Player;
import werewolf.game.WerewolfGame;
import werewolf.net.Command;

public class CmdInjure extends GameCommand
{

	public CmdInjure(WerewolfGame game)
	{
		super(game);
		this.name = "injure";
		this.info = "Injures a target player for a set number of rounds. Defaults to 1 round, but a specific # may be added. Only usable by a host";
		this.usage = "player[, rounds]";
		this.match = "injure|hospitalize";
		this.mustBeTrue = new Requirement[]
		{ Requirement.HOST };
	}

	@Override
	protected boolean execute(Command cmd)
	{
		try
		{
			List<String> params = cmd.getParams(2);
			Player target = this.game.getPlayer(params.get(0));
			if (target == null)
			{
				cmd.invalidate("invalid player");
				return false;
			}
			int rounds = 1;
			if (params.size() > 1)
				rounds = Integer.parseInt(params.get(1));
			if (rounds < 1) {
				cmd.invalidate("must injure for at least one round");
				return false;
			}

			target.injure(rounds);
		} catch (NumberFormatException ex)
		{
			cmd.invalidate("a non-number was supplied after the player");
			return false;
		}
		return true;
	}
}
