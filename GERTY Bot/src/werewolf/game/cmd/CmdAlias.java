package werewolf.game.cmd;

import java.util.List;

import werewolf.game.WerewolfGame;
import werewolf.net.Command;
import werewolf.net.ForumUser;

public class CmdAlias extends GameCommand
{

	public CmdAlias(WerewolfGame game)
	{
		super(game);
		this.name = "alias";
		this.info = "Adds a new alias to a given user. Users can be specified by id or current name. The alias may not contain commas or be exactly matched by any other known user or alias.";
		this.match = "alias";
		this.usage = "user, string";
	}

	@Override
	protected boolean execute(Command cmd) throws InvalidatonException
	{
		List<String> params = cmd.getParams(3);
		ForumUser aliasPlayer = getUser(params.get(0));
		String alias = params.get(1);
		ForumUser existingAlias = this.game.getUser(alias);
		if (existingAlias != null)
			throw new InvalidatonException("alias conflicts with " + existingAlias.getName());
		aliasPlayer.addAlias(alias);
		return false;
	}
}
