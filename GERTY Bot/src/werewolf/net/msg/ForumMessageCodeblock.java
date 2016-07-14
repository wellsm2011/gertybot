package werewolf.net.msg;

public class ForumMessageCodeblock extends ForumMessageElement
{
	public ForumMessageCodeblock(ForumMessageElement... children)
	{
		super(children);
	}

	public ForumMessageCodeblock(String msg)
	{
		this(new ForumMessageString(msg));
	}

}
