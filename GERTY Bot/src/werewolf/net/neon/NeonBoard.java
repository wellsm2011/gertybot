package werewolf.net.neon;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import werewolf.net.ForumBoard;
import werewolf.net.ForumContext;
import werewolf.net.ForumThread;

public class NeonBoard extends ForumBoard
{
	private static final long serialVersionUID = 8482450920548956509L;

	/**
	 * Loads data for a new board on NeonDragon.net.
	 *
	 * @param boardId
	 */
	public NeonBoard(int boardId)
	{
		super(boardId + "");
	}

	/**
	 * @return The forum context of this board.
	 */
	@Override
	public ForumContext getContext()
	{
		return NeonContext.INSTANCE;
	}

	/**
	 * @return A list of the threads contained in this forum board.
	 * @throws IOException
	 */
	@Override
	protected List<ForumThread> loadBoard() throws IOException
	{
		List<ForumThread> newThreads = new LinkedList<>();

		HtmlPage page = this.getContext().getBoardPage(this.boardId + "");
		List<?> threadTable = page.getByXPath("//div[@id='pagecontent']/table[@class='tablebg']/tbody/tr/td[2]/a[last()]");
		for (int i = 0; i < threadTable.size(); i++)
		{
			HtmlAnchor threadLink = (HtmlAnchor) threadTable.get(i);
			int threadId = Integer.parseInt(threadLink.getAttribute("href").replaceAll(".*\\&t=", "").replaceAll("\\&sid=.*", ""));
			String title = threadLink.asText();
			boolean sticky = ((DomElement) threadLink.getParentNode().getParentNode()).getAttribute("class").equals("topicsticky");
			boolean found = false;

			for (ForumThread thread : this.threads)
				if (thread.getThreadId() == "" + threadId)
				{
					found = true;
					thread.markStickied(sticky);
					newThreads.add(thread);
				}

			if (!found)
				newThreads.add(NeonThread.getThread(Integer.parseInt(this.boardId), threadId, title, sticky));
		}
		return newThreads;
	}
}
