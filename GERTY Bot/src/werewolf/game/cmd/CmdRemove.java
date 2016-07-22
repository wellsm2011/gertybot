package werewolf.game.cmd;

import werewolf.game.Player;
import werewolf.game.WerewolfGame;
import werewolf.net.Command;

public class CmdRemove extends GameCommand
{
	public CmdRemove(WerewolfGame game)
	{
		super(game);
		this.name = "remove";
		this.info = "Removes a player from the game. Only usable by a host";
		this.usage = "player";
		this.match = "remove";
		this.mustBeTrue = new Requirement[]
		{ Requirement.HOST, Requirement.PLAYER };
	}

	@Override
	protected boolean execute(Command cmd)
	{
		Player target = this.game.getPlayer(cmd.getParamString());
		if (target == null)
		{
			cmd.invalidate("unknown player");
			return false;
		}

		// TODO - Finish when there's a remove command
		// this.game.;
		return true;
	}
}
