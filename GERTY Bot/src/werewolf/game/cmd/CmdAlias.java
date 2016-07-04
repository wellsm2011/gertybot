package werewolf.game.cmd;

import werewolf.game.WerewolfGame;
import werewolf.game.cmd.GameCommand.Requirement;
import werewolf.net.Command;
import werewolf.net.ForumUser;

public class CmdAlias extends GameCommand
{

	public CmdAlias(WerewolfGame game)
	{
		super(game);
		this.name = "Alias";
		this.info = "Adds a new alias to a given player. Player can be specified by id or current name. The alias may not contain commas or be exactly matched by any other known user or alias.";
		this.match = "alias";
		this.mustBeTrue = new Requirement[]
		{ Requirement.HOST };
	}

	@Override
	protected boolean execute(Command cmd)
	{
		String[] params = cmd.getParams();
		ForumUser aliasPlayer = game.getContext().getUserDatabase().getUserFromExternalSource(params[0]);

		return false;
	}

}
