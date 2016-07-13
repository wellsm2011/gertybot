package werewolf.game.cmd;

import java.util.List;

import werewolf.game.Player;
import werewolf.game.WerewolfGame;
import werewolf.game.cmd.GameCommand.Requirement;
import werewolf.net.Command;
import werewolf.net.ForumUser;
import werewolf.net.msg.ForumMessageString;

public class CmdRevive extends GameCommand
{
	public CmdRevive(WerewolfGame game)
	{
		super(game);
		this.name = "revive";
		this.info = "Revives a deceased player. Only usable by the host.";
		this.usage = "player[, message]"; // Turns into: revive player[, message]
		this.match = "revive|raise|unkill";
		this.mustBeTrue = new Requirement[]
		{ Requirement.HOST };
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
		if (target.isAlive())
		{
			cmd.invalidate("target is alive");
			return false;
		}
		String msg = "Revived";
		if (params.size() > 1)
			msg = params.get(1); // Second param.
		this.game.getPlayer(target).revive(new ForumMessageString(msg), game.getRound(), cmd.getPost());

		return true;
	}

}
