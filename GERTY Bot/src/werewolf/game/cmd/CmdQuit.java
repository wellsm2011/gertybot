package werewolf.game.cmd;

import werewolf.game.Player;
import werewolf.game.WerewolfGame;
import werewolf.net.ParsedCommand;
import werewolf.net.Message;

public class CmdQuit extends GameCommand
{
	public CmdQuit(WerewolfGame game)
	{
		super(game);
		this.name = "quit";
		this.info = Message.of("Removes you from the game. Can only be used during Pregame.");
		this.usage = "";
		this.match = "quit|out|leave";
		this.mustBeTrue = new Requirement[]
		{ Requirement.PLAYER };
	}

	@Override
	protected boolean execute(ParsedCommand cmd) throws InvalidatonException
	{
		Player target = this.getPlayer(cmd.getUser());
		this.game.getPlayers().add(new Player(target, cmd.getPost(), this.game));
		return true;
	}
}
