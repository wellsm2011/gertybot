package werewolf.experimental;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;

import werewolf.net.msg.ForumMessageString;

public abstract class ForumMessageElement
{

	public enum ElementStyle
	{
		/**
		 * test
		 */
		BOLD(""),
		CODEBLOCK(""),
		/**
		 * test3
		 */
		COLOR(""),
		/**
		 * test2
		 */
		QUOTE("");
		private ElementStyle(String txt)
		{

		}
	}

	protected List<ForumMessageElement> children = new LinkedList<>();

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
