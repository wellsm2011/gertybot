package werewolf.net.msg;

public class ForumMessageSpoiler extends ForumMessageElement
{
	String title;

	public ForumMessageSpoiler(ForumMessageElement... children)
	{
		this("", children);
	}

	public ForumMessageSpoiler(String msg)
	{
		this("", msg);
	}

	public ForumMessageSpoiler(String author, ForumMessageElement... children)
	{
		super(children);
		this.title = author;
	}

	public ForumMessageSpoiler(String author, String msg)
	{
		this(author, new ForumMessageString(msg));
	}

	public String getTitle()
	{
		return this.title;
	}
}
