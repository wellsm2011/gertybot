package werewolf.game.cmd;

import werewolf.game.WerewolfGame;
import werewolf.net.Command;

public class CmdCohost extends GameCommand
{

	public CmdCohost(WerewolfGame game)
	{
		super(game);
	}

	@Override
	protected boolean execute(Command cmd)
	{
		return false;
		// TODO Auto-generated method stub

	}

}
