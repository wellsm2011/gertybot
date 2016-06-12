package werewolf.net.neon;


import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlStrong;

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


public class NeonThread extends ForumThread {
    @SuppressWarnings("compatibility:-6272710298360392184")
    private static final long serialVersionUID = 2939364857283333100L;

    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormat.forPattern("E MMMM d, YYYY h:m a");

    private static LinkedList<NeonThread> loadedThreads = new LinkedList<NeonThread>();

    private String lastPage = "";


    public static NeonThread getThread(int boardId, int threadId) {
        for (NeonThread thread : loadedThreads) {
            if (thread.getThreadId().equals("" + threadId))
                return thread;
        }
        NeonThread thread = new NeonThread(boardId, threadId);
        loadedThreads.add(thread);
        return thread;
    }
    
    public static NeonThread getThread(int threadId) {
        return getThread(0, threadId);
    }
    
    
    public static NeonThread getThread(int boardId, int threadId, String title, boolean isStickied) {
        for (NeonThread thread : loadedThreads) {
            if (thread.getThreadId().equals("" + threadId)) {
                thread.setStickied(isStickied);
                return thread;
            }
        }
        NeonThread thread = new NeonThread(boardId, threadId, title, isStickied);
        loadedThreads.add(thread);
        return thread;
    }    

    public static NeonThread getThread(int threadId, String title, boolean isStickied) {
        return getThread(0, threadId, title, isStickied);
    }
    


    private NeonThread(int boardId, int threadId) {
        super((boardId <= 0 ? "" : "" + boardId), threadId + "");
    }

    private NeonThread(int boardId, int threadId, String title, boolean isStickied) {
        super((boardId <= 0 ? "" : "" + boardId), threadId + "", title, isStickied, false, true);
    }


    @Override
    protected String parseThreadTitle(HtmlPage page) {
        return ((HtmlAnchor)page.getByXPath("//h2/a[@class='titles']").get(0)).asText();
    }


    @Override
    protected boolean isValidThreadPage(HtmlPage page) {
        if (page.asText().equals(lastPage))
            return false;
        lastPage = page.asText();
        return true;
    }


    @Override
    protected void parsePage(HtmlPage page) {
        String  tableRef = "//div[@id='pagecontent']/table[@class='tablebg'][last()]/tbody/tr";
        List<?> postTable = page.getByXPath(tableRef);
        List<?> spoilerLinks = page.getByXPath("//a[text()='show']");
        for (int i = 0; i < spoilerLinks.size(); ++i) {
            try {
                HtmlAnchor link = (HtmlAnchor)spoilerLinks.get(i);
                link.click();
            } catch (IOException e) {}
        }
        for (int i = 2; i < postTable.size(); i += 3) {
            String     postRow = tableRef + "[" + (i) + "]/td";
            String     dataRow = tableRef + "[" + (i + 1) + "]/td";
            String     posterName = ((HtmlDivision)page.getFirstByXPath(postRow + "/div[@class='postauthor']")).asText();
            HtmlAnchor postLink = ((HtmlAnchor)page.getFirstByXPath(postRow + "/div[@class='postsubject']/a"));
            int        postId = Integer.parseInt(postLink.getAttribute("href").replaceAll(".*#p", ""));
            HtmlAnchor posterLink = ((HtmlAnchor)page.getFirstByXPath(dataRow + "/a/img[@title='Profile']/.."));
            ForumUser  poster;

            if (posterLink != null) {
                int posterId = Integer.parseInt(posterLink.getAttribute("href").replaceAll(".*u=", ""));
                poster = ForumUser.getUserFor(posterId, posterName, getContext());
                poster.setName(posterName);
            }
            else
                poster = ForumUser.getUserFor(posterName, getContext());
            boolean            editable = page.getByXPath(postRow + "//a/img[@title='Edit post']").size() > 0;
            ArrayList<Command> commands = new ArrayList<Command>();
            List<?>            commandElements = page.getByXPath(postRow + "//div[@class='postbody']/strong");
            boolean            edited = page.getByXPath(postRow + "//div[@class='edited']").size() > 0;
            String             postBody = ((HtmlDivision)page.getFirstByXPath(postRow + "//div[@class='postbody']")).asText();

            HtmlElement timeStamp = page.getFirstByXPath(dataRow + "[@class='postbottom']");

            DateTime postTime = DateTime.parse(timeStamp.asText(), TIME_FORMAT);

            NeonPost post = new NeonPost(this, postId, postTime, poster, commands, editable, postBody);
            for (Object e : commandElements) {
                for (DomNode element : ((HtmlStrong)e).getChildren()) {
                    if (!element.getNodeName().equals("#text"))
                        element.remove();
                }
                String command = ((HtmlStrong)e).asText();
                if (command.matches("\\[\\[?.*\\]\\]?"))
                    commands.add(new Command(command, poster, edited, false, post));
            }

            addPost(post);
        }
    }


    @Override
    public ForumContext getContext() {
        return NeonContext.INSTANCE;
    }
}
