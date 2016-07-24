package werewolf.game.cmd;

import java.util.List;

import werewolf.game.Player;
import werewolf.game.WerewolfGame;
import werewolf.net.Command;
import werewolf.net.Message;

public class CmdInjure extends GameCommand
{

	public CmdInjure(WerewolfGame game)
	{
		super(game);
		this.name = "injure";
		this.info = Message.of("Injures a target player for a set number of rounds. Defaults to 1 round, but a specific # may be added. Only usable by a host");
		this.usage = "player[, rounds]";
		this.match = "injure|hospitalize";
		this.mustBeTrue = new Requirement[]
		{ Requirement.HOST };
	}

	@Override
	protected boolean execute(Command cmd) throws InvalidatonException
	{
		List<String> params = cmd.getParams(2);
		Player target = this.getPlayer(params.get(0));
		int rounds = 1;
		if (params.size() > 1)
			rounds = this.getInteger(params.get(1));
		if (rounds < 1)
			throw new InvalidatonException("must injure for at least one round");

		target.injure(rounds);
		return true;
	}
}
