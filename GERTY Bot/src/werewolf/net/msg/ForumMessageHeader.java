package werewolf.net.msg;

public class ForumMessageHeader extends ForumMessageElement
{
	public ForumMessageHeader(ForumMessageElement... children)
	{
		super(children);
	}

	public ForumMessageHeader(String msg)
	{
		this(new ForumMessageString(msg));
	}
}
