package werewolf.game.cmd;

import werewolf.game.Player;
import werewolf.game.WerewolfGame;
import werewolf.net.Command;
import werewolf.net.ForumUser;

public class CmdAdd extends GameCommand
{
	public CmdAdd(WerewolfGame game)
	{
		super(game);
		this.name = "Add";
		this.info = "Adds a new player to the game. Only usable by the host";
		this.match = "add";
		this.mustBeTrue = new Requirement[]
		{ Requirement.HOST };
	}

	@Override
	protected boolean execute(Command cmd)
	{
		ForumUser target = game.getUser(cmd.getParamString());
		if (target == null)
		{
			cmd.invalidate("unknown user");
			return false;
		}
		if (game.getPlayer(target) != null)
		{
			cmd.invalidate("target is a player");
			return false;
		}
		game.getPlayers().add(new Player(target, cmd.getPost(), game));
		return true;
	}

}
