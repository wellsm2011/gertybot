package werewolf.net.msg;

public class ForumMessageItalic extends ForumMessageElement
{
	public ForumMessageItalic(ForumMessageElement... children)
	{
		super(children);
	}

	public ForumMessageItalic(String msg)
	{
		this(new ForumMessageString(msg));
	}
}
