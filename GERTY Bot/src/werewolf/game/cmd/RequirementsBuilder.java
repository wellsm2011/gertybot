package werewolf.game.cmd;

import java.util.function.BiFunction;

import werewolf.game.GamePhase;
import werewolf.game.Player;
import werewolf.game.WerewolfGame;
import werewolf.game.cmd.GameCommand.Requirement;
import werewolf.net.ForumUser;
import werewolf.net.ParsedCommand;

public class RequirementsBuilder
{
	protected static class UnmetRequirementException extends Exception
	{
		private static final long serialVersionUID = 1L;

		public UnmetRequirementException(String msg)
		{
			super(msg);
		}
	}

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

	protected class Req
	{

	}

	public class Requirements
	{

		public void validateAndParse(WerewolfGame game, ParsedCommand cmd) throws UnmetRequirementException
		{

		}

		public <T> T getParam(int index)
		{
			return null;
		}

	}

	public RequirementsBuilder addParam(String name, Type t)
	{

		return this;
	}

	public RequirementsBuilder addParams(String name, Type t, int count)
	{

		return this;
	}

	public Requirements addParams(String name, Type t)
	{

		return this.createSet();
	}

	private Requirements createSet()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
