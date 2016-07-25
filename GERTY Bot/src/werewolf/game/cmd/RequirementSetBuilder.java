package werewolf.game.cmd;

import werewolf.game.Player;
import werewolf.game.WerewolfGame;
import werewolf.net.ForumUser;
import werewolf.net.ParsedCommand;

public class RequirementSetBuilder
{
	public enum Type
	{
		STRING(String.class),
		FORUM_USER(ForumUser.class),
		PLAYER(Player.class),
		INTEGER(Integer.class);

		public final Class<?> type;

		private Type(Class<?> type)
		{
			this.type = type;
		}
	}

	public class RequirementSet
	{



		public <T> T getParam(int index)
		{
			
		}

	}

	public class RequirementSetParser
	{
		public RequirementSet parse(WerewolfGame game, ParsedCommand cmd)
		{
			
			return null;
		}
		public boolean validate(WerewolfGame game, ParsedCommand cmd)
		{
			// TODO this
			return false;
		}
	}

	public RequirementSetBuilder addParam(String name, Type t)
	{

		return this;
	}

	public RequirementSetBuilder addParams(String name, Type t, int count)
	{

		return this;
	}

	public RequirementSet addParams(String name, Type t)
	{

		return this.createSet();
	}

	private RequirementSet createSet()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
