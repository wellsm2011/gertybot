package werewolf.experimental;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

import werewolf.net.msg.ForumMessageString;

public class ForumMessageElement
{

	public enum StyleFlags
	{
		BOLD,
		CODEBLOCK,
		COLOR,
		QUOTE,
		SPOILER;
	}

	private List<ForumMessageElement>	children	= new LinkedList<>();
	private Set<StyleFlags>				flags		= new HashSet<>();

	public ForumMessageElement()
	{
	}

	public ForumMessageElement(ForumMessageElement[] children)
	{
		for (ForumMessageElement elm : children)
			this.children.add(elm);
	}

	public ForumMessageElement append(ForumMessageElement elm)
	{
		this.children.add(elm);
		return this;
	}

	public ForumMessageElement append(String msg)
	{
		this.children.add(new ForumMessageString(msg));
		return this;
	}

	public List<ForumMessageElement> getChildren()
	{
		return this.children;
	}

	public String toString(BiFunction<ForumMessageElement, String, String> encoder)
	{
		String inner = "";
		for (ForumMessageElement child : this.children)
			inner += child.toString(encoder);
		return encoder.apply(this, inner);
	}
}
