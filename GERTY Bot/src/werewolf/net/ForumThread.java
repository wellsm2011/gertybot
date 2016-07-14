package werewolf.net;

import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import werewolf.net.msg.ForumMessageElement;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

public abstract class ForumThread implements Serializable
{
	private static final Logger		LOGGER				= Logger.getLogger(ForumThread.class.getName());
	private static final long		serialVersionUID	= -1204239372191855699L;

	protected LinkedList<ForumPost>	posts				= new LinkedList<ForumPost>();
	protected LinkedList<ForumPost>	deleted				= new LinkedList<ForumPost>();
	protected LinkedList<ForumPost>	edited				= new LinkedList<ForumPost>();

	private boolean					stickied			= false;
	private boolean					locked				= false;
	private boolean					editable			= false;
	private String					threadId;
	private String					boardId;
	private String					title				= null;

	private boolean					initalized			= false;
	private int						postReadIndex		= 0;
	private int						pagesParsed			= 0;

	protected ForumThread(String boardId, String threadId)
	{
		this.boardId = boardId;
		this.threadId = threadId;
	}

	protected ForumThread(String boardId, String threadId, boolean stickied, boolean locked, boolean editable)
	{
		this(boardId, threadId);
		this.stickied = stickied;
		this.locked = locked;
		this.editable = editable;
	}

	protected ForumThread(String boardId, String threadId, String title)
	{
		this(boardId, threadId);
		this.title = title;
	}

	protected ForumThread(String boardId, String threadId, String title, boolean stickied, boolean locked, boolean editable)
	{
		this(boardId, threadId, stickied, locked, editable);
		this.title = title;
	}

	protected void addPost(ForumPost post)
	{
		int index = this.getPostIndex(post);
		if (index >= 0)
		{
			if (!post.equalCommands(this.posts.get(index)))
				this.edited.add(post);
			this.posts.set(index, post);
		} else
			this.posts.add(post);
	}

	public boolean equals(ForumThread o)
	{
		return this.getThreadId().equals(o.getThreadId()) && this.getContext().equals(o.getContext());
	}

	public String getBoardId()
	{
		return this.boardId;
	}

	public abstract ForumContext getContext();

	public int getPostIndex(ForumPost post)
	{
		try
		{
			if (!this.initalized)
				this.refreshAll();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		for (int i = 0; i < this.posts.size(); ++i)
			if (this.posts.get(i).equals(post))
				return i;
		return -1;
	}

	public List<ForumPost> getPosts() throws IOException
	{
		if (!this.initalized)
			this.refreshAll();
		return this.posts;
	}

	public String getThreadId()
	{
		return this.threadId;
	}

	public String getTitle() throws IOException
	{
		if (this.title == null)
			this.refreshAll();
		return this.title;
	}

	public String getUrl() throws IOException
	{
		return this.getContext().getThreadUrl(this.boardId, this.threadId);
	}

	public boolean isEditable()
	{
		return this.editable;
	}

	public boolean isLocked()
	{
		return this.locked;
	}

	public boolean isStickied()
	{
		return this.stickied;
	}

	protected abstract boolean isValidThreadPage(HtmlPage page);

	public void markEditable(boolean editable)
	{
		this.editable = editable;
	}

	public void markLocked(boolean locked)
	{
		this.locked = locked;
	}

	public void markStickied(boolean stickied)
	{
		this.stickied = stickied;
	}

	public ForumPost nextPost() throws IOException
	{
		if (!this.initalized)
			this.refreshAll();
		if (this.postReadIndex >= this.posts.size())
			return null;
		return this.posts.get(this.postReadIndex++);
	}

	public int pages() throws IOException
	{
		if (!this.initalized)
			this.refreshAll();
		return (int) Math.ceil(this.posts.size() / 15.0);
	}

	protected abstract void parsePage(HtmlPage page);

	protected abstract String parseThreadTitle(HtmlPage page);

	public void post(ForumMessageElement message) throws IOException
	{
		this.getContext().makePost(this.getContext().getThreadReplyPage(this.boardId, this.threadId), message);
	}

	public void refresh() throws IOException
	{
		if (!this.initalized)
		{
			if (this.title != null && !this.title.isEmpty())
				ForumThread.LOGGER.info("Loading new thread (context=" + this.getContext() + "): " + this.title);
			else
				ForumThread.LOGGER.info("Loading new thread (context=" + this.getContext() + ")...");
		} else
			ForumThread.LOGGER.info("Reloading Thread (context=" + this.getContext() + "): " + this.title);
		this.initalized = true;

		try
		{
			HtmlPage page = this.getContext().getThreadPage(this.boardId, this.threadId, this.pagesParsed);
			this.title = this.parseThreadTitle(page);

			do
			{
				this.parsePage(page);
				this.getContext().CLIENT.close();
				page = this.getContext().getThreadPage(this.boardId, this.threadId, ++this.pagesParsed);
			} while (this.isValidThreadPage(page));

			this.pagesParsed -= 1;
			this.getContext().CLIENT.close();
		} catch (Exception ex)
		{
			this.initalized = false;
			throw ex;
		}

		int commands = 0;
		for (ForumPost post : this.posts)
			commands += post.getCommands().size();

		ForumThread.LOGGER.info("Thread loaded (context=" + this.getContext() + "): " + this.title + ". " + this.posts.size() + " posts and " + commands + " commands found.");
	}

	public void refreshAll() throws IOException
	{
		this.pagesParsed = 1;
		this.refresh();
	}

	public void reset() throws IOException
	{
		this.refreshAll();
		this.postReadIndex = 0;
	}

	@Override
	public String toString()
	{
		String status = "";
		if (this.stickied)
			status += ", stickied";
		if (this.locked)
			status += ", locked";

		if (this.title.isEmpty())
			return "[id=" + this.threadId + ", title=unparsed]";
		return "[id=" + this.threadId + ", title=" + this.title + status + "]";
	}
}
