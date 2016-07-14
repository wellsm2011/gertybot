package werewolf.game.cmd;

import java.util.List;

import werewolf.game.Player;
import werewolf.game.WerewolfGame;
import werewolf.net.Command;
import werewolf.net.msg.ForumMessageString;

public class CmdModkill extends GameCommand
{
	public CmdModkill(WerewolfGame game)
	{
		super(game);
		this.name = "modkill";
		this.info = "Modkills a target player. Only usable by a host";
		this.usage = "player[, message]";
		this.match = "modkill";
		this.mustBeTrue = new Requirement[]
		{ Requirement.HOST, Requirement.PLAYER };
	}

	@Override
	protected boolean execute(Command cmd)
	{
		List<String> params = cmd.getParams(2);
		Player target = this.game.getPlayer(params.get(0));
		if (target == null)
		{
			cmd.invalidate("unknown player");
			return false;
		}
		String msg = "Modkilled";
		if (params.size() > 1)
			msg = params.get(1); // Second param.
		target.kill(new ForumMessageString(msg), this.game.getRound(), cmd.getPost());
		target.setModkilled(true);
		return true;
	}

}
