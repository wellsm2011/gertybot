package werewolf.game.cmd;

import java.util.LinkedList;
import java.util.List;

import werewolf.game.WerewolfGame;
import werewolf.net.Command;
import werewolf.net.ForumMessage;
import werewolf.net.ForumUser;

public class CmdCoHost extends GameCommand
{

	public CmdCoHost(WerewolfGame game)
	{
		super(game);
		this.name = "cohost";
		this.info = ForumMessage.of("Designates user(s) as the cohost(s), or removes all cohosts if blank. Only usable by a host");
		this.usage = "user[, ...]";
		this.match = "cohost";
		this.mustBeTrue = new Requirement[]
		{ Requirement.HOST };
	}

	@Override
	protected boolean execute(Command cmd)
	{
		List<String> params = cmd.getParams(this.game.getPlayers().size() + 1);
		List<ForumUser> cohosts = new LinkedList<>();
		// Check to see if we're removing all cohosts.
		if (params.get(0).length() == 0)
			this.game.setCohosts(new LinkedList<ForumUser>());
		else
		{
			// Read each new cohost.
			for (String user : params)
			{
				ForumUser tempUser = this.game.getUser(user);
				if (tempUser == null)
				{
					cmd.invalidate("unknown user");
					return false;
				}
				if (!cohosts.contains(tempUser))
					cohosts.add(tempUser);
			}
			this.game.setCohosts(cohosts);
		}
		return true;
	}
}