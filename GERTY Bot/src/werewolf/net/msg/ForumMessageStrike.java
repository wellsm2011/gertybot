package werewolf.net.msg;

public class ForumMessageStrike extends ForumMessageElement
{
	public ForumMessageStrike(String msg)
	{
		this(new ForumMessageString(msg));
	}

	public ForumMessageStrike(ForumMessageElement... children)
	{
		super(children);
	}

}
