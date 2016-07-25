package werewolf.game.cmd;

import werewolf.game.WerewolfGame;
import werewolf.game.cmd.GameCommand.Requirement;
import werewolf.net.ParsedCommand;
import werewolf.net.Message;

public class CmdStory extends GameCommand
{

	public CmdStory(WerewolfGame game)
	{
		super(game);
		this.name = "story";
		this.info = Message.of("Revives a deceased player. Only usable by a host.");
		this.usage = "[title]";
		this.match = "story(post)?|flag|title";
		this.mustBeTrue = new Requirement[]
		{ Requirement.HOST };
		this.mustBeFalse = new Requirement[]
		{ Requirement.ALIVE };
	}

	@Override
	protected boolean execute(ParsedCommand cmd) throws InvalidatonException, IndexOutOfBoundsException
	{
		
	}
}
