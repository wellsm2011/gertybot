package werewolf.game.cmd;

import java.util.List;

import werewolf.game.Player;
import werewolf.game.WerewolfGame;
import werewolf.net.ParsedCommand;
import werewolf.net.Message;

public class CmdModkill extends GameCommand
{
	public CmdModkill(WerewolfGame game)
	{
		super(game);
		this.name = "modkill";
		this.info = Message.of("Modkills a target player. Only usable by a host");
		this.usage = "player[, message]";
		this.match = "modkill";
		this.mustBeTrue = new Requirement[]
		{ Requirement.HOST, Requirement.PLAYER };
	}

	@Override
	protected boolean execute(ParsedCommand cmd) throws InvalidatonException
	{
		List<String> params = cmd.getParams(2);
		Player target = this.getPlayer(params.get(0));
		String msg = "Modkilled";
		if (params.size() > 1)
			msg += " " + params.get(1); // Second param.
		target.kill(Message.of(msg), this.game.getRound(), cmd.getPost());
		target.setModkilled(true);
		return true;
	}

}
