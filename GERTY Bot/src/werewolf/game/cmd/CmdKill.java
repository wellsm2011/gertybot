package werewolf.game.cmd;

import java.util.List;

import werewolf.game.Player;
import werewolf.game.WerewolfGame;
import werewolf.net.Command;
import werewolf.net.ForumMessage;

public class CmdKill extends GameCommand
{
	public CmdKill(WerewolfGame game)
	{
		super(game);
		this.name = "kill";
		this.info = ForumMessage.of("Kills a target player. Only usable by a host");
		this.usage = "player[, reason]";
		this.match = "kill";
		this.mustBeTrue = new Requirement[]
		{ Requirement.HOST, Requirement.ALIVE };
	}

	@Override
	protected boolean execute(Command cmd) throws InvalidatonException
	{
		List<String> params = cmd.getParams(2);
		Player target = this.getLivingPlayer(params.get(0));
		String msg = "Killed";
		if (params.size() > 1)
			msg = params.get(1);
		target.kill(ForumMessage.of(msg), this.game.getRound(), cmd.getPost());
		return true;
	}
}
