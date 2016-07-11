package werewolf.net.msg;

public class ForumMessageItalic extends ForumMessageElement
{
	public ForumMessageItalic(String msg)
	{
		this(new ForumMessageString(msg));
	}

	public ForumMessageItalic(ForumMessageElement ... children)
	{
		super(children);
	}
}
