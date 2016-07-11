package werewolf.net.msg;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;

public abstract class ForumMessageElement
{
	protected LinkedList<ForumMessageElement>	children	= new LinkedList<>();

	public ForumMessageElement(ForumMessageElement[] children)
	{
		for (ForumMessageElement elm : children)
			this.children.add(elm);
	}

	public ForumMessageElement()
	{
	}

	public ForumMessageElement append(ForumMessageElement elm)
	{
		children.add(elm);
		return this;
	}

	public ForumMessageElement append(String msg)
	{
		children.add(new ForumMessageString(msg));
		return this;
	}

	public List<ForumMessageElement> getChildren()
	{
		return children;
	}

	public String toString(BiFunction<ForumMessageElement, String, String> encoder)
	{
		String inner = "";
		for (ForumMessageElement child : children)
			inner += child.toString(encoder);
		return encoder.apply(this, inner);
	}
}
