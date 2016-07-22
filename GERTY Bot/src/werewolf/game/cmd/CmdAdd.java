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
		this.name = "add";
		this.info = "Adds a new player to the game. Only usable by a host";
		this.usage = "user";
		this.match = "add";
		this.mustBeTrue = new Requirement[]
		{ Requirement.HOST };
		this.mustBeFalse = new Requirement[]
		{ Requirement.PLAYER };
	}

	@Override
	protected boolean execute(Command cmd) throws InvalidatonException
	{
		ForumUser target = this.getNonPlayer(cmd.getParams(1).get(0));
		this.game.getPlayers().add(new Player(target, cmd.getPost(), this.game));
		return true;
	}
}
