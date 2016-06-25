package werewolf.net;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.joda.time.DateTime;

public abstract class ForumPost implements Serializable
{
	private static final long	serialVersionUID	= 1043955423819856116L;

	protected ForumUser			poster;
	protected boolean			isEditable;
	protected boolean			edited				= false;
	protected boolean			deleted				= false;
	protected List<Command>		commands;
	protected int				postId;
	protected String			content;
	protected ForumThread		thread;
	protected DateTime			postTime;

	public ForumPost(ForumThread parent, int postId, DateTime postTime, ForumUser poster, List<Command> commands, boolean isEditable, String content)
	{
		this.poster = poster;
		this.thread = parent;
		this.commands = commands;
		this.postId = postId;
		this.isEditable = isEditable;
		this.content = content;
		this.postTime = postTime;
	}

	public void delete() throws IOException
	{
		if (this.deleted)
			return;
		this.executeDelete();
		this.deleted = true;
	}

	public boolean equalCommands(ForumPost toCompare)
	{
		if (this.commands.size() != toCompare.commands.size())
			return false;
		for (int i = 0; i < this.commands.size(); ++i)
			if (!this.commands.get(i).equals(toCompare.commands.get(i)))
				return false;
		return true;
	}

	@Override
	public boolean equals(Object o)
	{
		if (o instanceof ForumPost)
		{
			ForumPost post = (ForumPost) o;
			if (post.getPostId() == this.getPostId() && post.getContext().equals(this.getContext()))
				return true;
		}
		return false;
	}

	protected abstract void executeDelete() throws IOException;

	public void externalDelete()
	{
		this.deleted = true;
	}

	public void externalEdit()
	{
		this.edited = true;
	}

	public String getBoardId()
	{
		return this.thread.getBoardId();
	}

	public List<Command> getCommands()
	{
		return this.commands;
	}

	public abstract ForumContext getContext();

	/**
	 * @return
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	public abstract ForumPostEditor getEditor() throws IllegalStateException, IOException;

	/**
	 * @return The page in the thread where this post is.
	 */
	public int getPage()
	{
		return (int) Math.ceil((this.thread.getPostIndex(this) + 1) / 15.0);
	}

	/**
	 * @return The ForumUser who created this post.
	 */
	public ForumUser getPoster()
	{
		return this.poster;
	}

	public int getPostId()
	{
		return this.postId;
	}

	/**
	 * @return The time this post was created.
	 */
	public DateTime getPostTime()
	{
		return this.postTime;
	}

	public String getRawText()
	{
		return this.content;
	}

	/**
	 * @return The ForumThread which this post belongs to.
	 */
	public ForumThread getThread()
	{
		return this.thread;
	}

	public String getThreadId()
	{
		return this.thread.getThreadId();
	}

	public String getUrl()
	{
		return this.getContext().getPostUrl(this.thread.getThreadId(), this.getPage(), this.postId);
	}

	public boolean hasBeenDeleted()
	{
		return this.deleted;
	}

	public boolean hasBeenEdited()
	{
		return this.edited;
	}

	/**
	 * @return True if the bot is able to edit this post.
	 */
	public boolean isEditable()
	{
		return this.isEditable;
	}
}
