package werewolf.game.cmd;

import werewolf.game.Player;
import werewolf.game.WerewolfGame;
import werewolf.game.cmd.GameCommand.Requirement;
import werewolf.net.Command;
import werewolf.net.ForumUser;

public class CmdQuit extends GameCommand
{
	public CmdQuit(WerewolfGame game)
	{
		super(game);
		this.name = "quit";
		this.info = "Removes you from the game. Can only be used during Pregame.";
		this.usage = "";
		this.match = "quit";
		this.mustBeTrue = new Requirement[]
		{ Requirement.PREGAME , Requirement.PLAYER };
	}

	@Override
	protected boolean execute(Command cmd)
	{
		Player target = this.game.getPlayer(cmd.getParamString());
		if (target == null)
		{
			cmd.invalidate("unknown user");
			return false;
		}
		if (this.game.getPlayer(target) != null)
		{
			cmd.invalidate("target is a player");
			return false;
		}
		this.game.getPlayers().add(new Player(target, cmd.getPost(), this.game));
		return true;
	}
}
