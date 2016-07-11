package werewolf.net.msg;

import java.util.function.Function;

public class ForumMessageBold extends ForumMessageElement
{
	ForumMessageElement	msg;

	public ForumMessageBold(ForumMessageElement msg)
	{
		this.msg = msg;
	}

	public ForumMessageBold(String msg)
	{
		this.msg = new ForumMessageString(msg);
	}

	public String getMsg(Function<ForumMessageElement, String> encoder)
	{
		return encoder.apply(msg);
	}
}
