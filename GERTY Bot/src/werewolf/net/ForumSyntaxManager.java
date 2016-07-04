package werewolf.net;

/**
 * Represents a method of handling rich text input which can convert to the
 * posting syntax of this forum context.
 * 
 * @author Michael
 */
public class ForumSyntaxManager
{
	public String bold(String text)
	{
		return text;
	}

	public String italic(String text)
	{
		return text;
	}

	public String underline(String text)
	{
		return text;
	}

	public String spoiler(String text)
	{
		return text;
	}

	public String url(String url, String text)
	{
		return text + "<" + url + ">";
	}

	public String large(String text)
	{
		return text;
	}

	public String small(String text)
	{
		return text;
	}

	public String center(String text)
	{
		return text;
	}

	public String quote(String author, String text)
	{
		return "Quote by " + author + ":\n" + text.replaceAll("\n", "\n\t");
	}

	public String escape(String text)
	{
		return text;
	}
}
