package werewolf.net.msg;

public class ForumMessageQuote extends ForumMessageElement
{
	String author;

	public ForumMessageQuote(ForumMessageElement... children)
	{
		this("", children);
	}

	public ForumMessageQuote(String msg)
	{
		this("", msg);
	}

	public ForumMessageQuote(String author, ForumMessageElement... children)
	{
		super(children);
		this.author = author;
	}

	public ForumMessageQuote(String author, String msg)
	{
		this(author, new ForumMessageString(msg));
	}

	public String getAuthor()
	{
		return this.author;
	}
}
