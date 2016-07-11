package werewolf.net.msg;

public class ForumMessageBold extends ForumMessageElement
{
	public ForumMessageBold(ForumMessageElement... msg)
	{
		super(msg);
	}

	public ForumMessageBold(String msg)
	{
		super(new ForumMessageElement[]
		{ new ForumMessageString(msg) });
	}
}
