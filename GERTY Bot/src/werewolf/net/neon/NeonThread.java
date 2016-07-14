package werewolf.net.neon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import werewolf.net.Command;
import werewolf.net.ForumContext;
import werewolf.net.ForumThread;
import werewolf.net.ForumUser;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlStrong;

public class NeonThread extends ForumThread
{
	private static final long				serialVersionUID	= 2939364857283333100L;

	private static final DateTimeFormatter	timeFormat			= DateTimeFormat.forPattern("E MMMM d, YYYY h:m a");
	private static LinkedList<NeonThread>	loadedThreads		= new LinkedList<NeonThread>();

	public static NeonThread getThread(int threadId)
	{
		return NeonThread.getThread(0, threadId);
	}

	public static NeonThread getThread(int boardId, int threadId)
	{
		for (NeonThread thread : NeonThread.loadedThreads)
			if (thread.getThreadId().equals("" + threadId))
				return thread;
		NeonThread thread = new NeonThread(boardId, threadId);
		NeonThread.loadedThreads.add(thread);
		return thread;
	}

	public static NeonThread getThread(int boardId, int threadId, String title, boolean isStickied)
	{
		for (NeonThread thread : NeonThread.loadedThreads)
			if (thread.getThreadId().equals("" + threadId))
			{
				thread.markStickied(isStickied);
				return thread;
			}
		NeonThread thread = new NeonThread(boardId, threadId, title, isStickied);
		NeonThread.loadedThreads.add(thread);
		return thread;
	}

	public static NeonThread getThread(int threadId, String title, boolean isStickied)
	{
		return NeonThread.getThread(-1, threadId, title, isStickied);
	}

	private String	lastPage	= "";

	private NeonThread(int boardId, int threadId)
	{
		super(boardId <= 0 ? "" : "" + boardId, threadId + "");
	}

	private NeonThread(int boardId, int threadId, String title, boolean isStickied)
	{
		super(boardId < 0 ? "" : "" + boardId, threadId + "", title, isStickied, false, true);
	}

	@Override
	public ForumContext getContext()
	{
		return NeonContext.INSTANCE;
	}

	@Override
	protected boolean isValidThreadPage(HtmlPage page)
	{
		if (page.asText().equals(this.lastPage))
			return false;
		this.lastPage = page.asText();
		return true;
	}

	@Override
	protected void parsePage(HtmlPage page)
	{
		String tableRef = "//div[@id='pagecontent']/table[@class='tablebg'][last()]/tbody/tr";
		List<?> postTable = page.getByXPath(tableRef);
		List<?> spoilerLinks = page.getByXPath("//a[text()='show']");
		for (int i = 0; i < spoilerLinks.size(); ++i)
			try
			{
				HtmlAnchor link = (HtmlAnchor) spoilerLinks.get(i);
				link.click();
			} catch (IOException e)
			{
			}
		for (int i = 2; i < postTable.size(); i += 3)
		{
			String postRow = tableRef + "[" + i + "]/td";
			String dataRow = tableRef + "[" + (i + 1) + "]/td";
			String posterName = ((HtmlDivision) page.getFirstByXPath(postRow + "/div[@class='postauthor']")).asText();
			HtmlAnchor postLink = (HtmlAnchor) page.getFirstByXPath(postRow + "/div[@class='postsubject']/a");
			int postId = Integer.parseInt(postLink.getAttribute("href").replaceAll(".*#p", ""));
			HtmlAnchor posterLink = (HtmlAnchor) page.getFirstByXPath(dataRow + "/a/img[@title='Profile']/..");
			ForumUser poster;

			if (posterLink != null)
			{
				int posterId = Integer.parseInt(posterLink.getAttribute("href").replaceAll(".*u=", ""));
				poster = ForumUser.getUserFor(posterId, posterName, this.getContext());
				poster.setName(posterName);
			} else
				poster = ForumUser.getUserFor(posterName, this.getContext());
			boolean editable = page.getByXPath(postRow + "//a/img[@title='Edit post']").size() > 0;
			ArrayList<Command> commands = new ArrayList<Command>();
			List<?> commandElements = page.getByXPath(postRow + "//div[@class='postbody']/strong");
			boolean edited = page.getByXPath(postRow + "//div[@class='edited']").size() > 0;
			String postBody = ((HtmlDivision) page.getFirstByXPath(postRow + "//div[@class='postbody']")).asText();

			HtmlElement timeStamp = page.getFirstByXPath(dataRow + "[@class='postbottom']");

			DateTime postTime = DateTime.parse(timeStamp.asText(), NeonThread.timeFormat);

			NeonPost post = new NeonPost(this, postId, postTime, poster, commands, editable, postBody);
			for (Object e : commandElements)
			{
				for (DomNode element : ((HtmlStrong) e).getChildren())
					if (!element.getNodeName().equals("#text"))
						element.remove();
				String command = ((HtmlStrong) e).asText();
				if (command.matches("\\[\\[?.*\\]\\]?"))
					commands.add(new Command(command, poster, edited, post));
			}

			this.addPost(post);
		}
	}

	@Override
	protected String parseThreadTitle(HtmlPage page)
	{
		return ((HtmlAnchor) page.getByXPath("//h2/a[@class='titles']").get(0)).asText();
	}
}
