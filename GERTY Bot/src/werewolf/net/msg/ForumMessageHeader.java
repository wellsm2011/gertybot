package werewolf.net.msg;

public class ForumMessageHeader extends ForumMessageElement
{
	public ForumMessageHeader(String msg)
	{
		this(new ForumMessageString(msg));
	}

	public ForumMessageHeader(ForumMessageElement... children)
	{
		super(children);
	}
}
