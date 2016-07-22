package werewolf.net.msg;

public class ForumMessageUrl extends ForumMessageElement
{
	private String url;

	public ForumMessageUrl(String url)
	{
		this(url, url);
	}

	public ForumMessageUrl(String url, ForumMessageElement... children)
	{
		super(children);
		this.url = url;
	}

	public ForumMessageUrl(String url, String text)
	{
		this(url, new ForumMessageString(text));
	}

	public String getUrl()
	{
		return this.url;
	}
}
