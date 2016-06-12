package werewolf.net.halolz;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;

import java.util.LinkedList;
import java.util.List;

import java.util.regex.Pattern;

import werewolf.net.ForumBoard;
import werewolf.net.ForumContext;
import werewolf.net.ForumThread;

public class HalolzBoard extends ForumBoard {
    @SuppressWarnings("compatibility:-1372178864822532821")
    private static final long serialVersionUID = 7017137519257021476L;

    public HalolzBoard(String string) {
        super(string);
    }

    /**
     * @return A list of the threads contained in this forum board.
     * @throws IOException
     */
    @Override
    protected LinkedList<ForumThread> loadBoard() throws IOException {
        LinkedList<ForumThread> newThreads = new LinkedList<ForumThread>();

        HtmlPage page = getContext().getBoardPage(boardId);
        List<?>  threadTable =
            page.getByXPath("//ul[@class='topiclist topics bg_none']/li/dl/dd[@class='dterm']/div/h2/a");
        List<?> statusTable = page.getByXPath("//ul[@class='topiclist topics bg_none']/li/dl");
        for (int i = 0; i < threadTable.size(); i++) {
            HtmlAnchor threadLink = (HtmlAnchor)threadTable.get(i);
            boolean    locked = ((DomElement)statusTable.get(i)).getAttribute("style").contains("i_folder_lock.gif");
            String     threadId = threadLink.getAttribute("href").replaceAll("/", "").replaceAll("\\&sid=.*", "");
            String     title = threadLink.asText();
            boolean    sticky = page.asText().matches("(Sticky|Announcement)\\: (\\[ Poll \\] *)?" + Pattern.quote(title));
            boolean    found = false;

            for (ForumThread thread : threads) {
                if (thread.getThreadId() == ("" + threadId)) {
                    found = true;
                    thread.setStickied(sticky);
                    newThreads.add(thread);
                }
            }

            if (!found)
                newThreads.add(new HalolzThread(boardId, threadId, title, sticky, locked));
        }
        return newThreads;
    }

    /**
     * @return The forum context of this board.
     */
    @Override
    public ForumContext getContext() {
        return HalolzContext.INSTANCE;
    }
}
