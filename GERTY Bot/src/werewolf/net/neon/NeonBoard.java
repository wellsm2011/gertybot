package werewolf.net.neon;


import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;

import java.util.LinkedList;
import java.util.List;

import werewolf.net.ForumBoard;
import werewolf.net.ForumContext;
import werewolf.net.ForumThread;


public class NeonBoard extends ForumBoard {
    @SuppressWarnings("compatibility:4652813682374509485")
    private static final long serialVersionUID = 8482450920548956509L;


    /**
     * Loads data for a new board on NeonDragon.net.
     *
     * @param boardId
     */
    public NeonBoard(int boardId) {
        super(boardId + "");
    }


    /**
     * @return A list of the threads contained in this forum board.
     * @throws IOException
     */
    @Override
    protected LinkedList<ForumThread> loadBoard() throws IOException {
        LinkedList<ForumThread> newThreads = new LinkedList<ForumThread>();

        HtmlPage page = getContext().getBoardPage(boardId + "");
        List<?>  threadTable =
            page.getByXPath("//div[@id='pagecontent']/table[@class='tablebg']/tbody/tr/td[2]/a[last()]");
        for (int i = 0; i < threadTable.size(); i++) {
            HtmlAnchor threadLink = (HtmlAnchor)threadTable.get(i);
            int        threadId =
                Integer.parseInt(threadLink.getAttribute("href").replaceAll(".*\\&t=", "").replaceAll("\\&sid=.*", ""));
            String  title = threadLink.asText();
            boolean sticky =
                ((DomElement)threadLink.getParentNode().getParentNode()).getAttribute("class").equals("topicsticky");
            boolean found = false;

            for (ForumThread thread : threads) {
                if (thread.getThreadId() == ("" + threadId)) {
                    found = true;
                    thread.setStickied(sticky);
                    newThreads.add(thread);
                }
            }

            if (!found)
                newThreads.add(NeonThread.getThread(Integer.parseInt(boardId), threadId, title, sticky));
        }
        return newThreads;
    }

    /**
     * @return The forum context of this board.
     */
    @Override
    public ForumContext getContext() {
        return NeonContext.INSTANCE;
    }
}


