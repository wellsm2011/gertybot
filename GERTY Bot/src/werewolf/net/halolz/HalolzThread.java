package werewolf.net.halolz;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlStrong;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import org.joda.time.format.DateTimeFormat;

import org.joda.time.format.DateTimeFormatter;

import werewolf.net.Command;

import werewolf.net.ForumContext;
import werewolf.net.ForumThread;
import werewolf.net.ForumUser;

public class HalolzThread extends ForumThread {
    @SuppressWarnings("compatibility:-2915009611789936521")
    private static final long serialVersionUID = -2196947748663761275L;

    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormat.forPattern("E MMMM d, YYYY h:m a");

    public HalolzThread(String boardId, String threadId) {
        super(boardId, threadId);
    }

    public HalolzThread(String boardId, String threadId, String title, boolean isStickied, boolean locked) {
        super(boardId, threadId, title, isStickied, locked, true);
    }


    protected void parsePage(HtmlPage page) {
        String  postXpath = "//div[@id='main-content']/div[contains(@class, 'post row')]";
        List<?> posts = page.getByXPath(postXpath);

        List<?> spoilerLinks = page.getByXPath("//dl[@class='codebox spoiler']/dt");
        for (int i = 0; i < spoilerLinks.size(); ++i) {
            try {
                HtmlElement link = (HtmlElement)spoilerLinks.get(i);
                link.click();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (int i = 1; i <= posts.size(); ++i) {
            String postString = "(" + postXpath + ")[" + i + "]";
            String contentString = postString + "//div[@class='content clearfix']";
            String contentPath = postString + "/div/div[@class='postbody']/div[@class='content clearfix']";
            String posterPath = postString + "//dt/strong/a";
            String commandPath = contentString + "/div/strong";
            String hiddenCommandPath =
                contentString + "/div/font[@color='#2e2e2f']/span[@style='font-size: 1px; line-height: normal']";
            String titlePath = postString + "//h2[@class='topic-title']/a";
            String editPath = postString + "//ul[@class='profile-icons']//a[contains(@href, 'mode=editpost')]";
            String editedPath = postString + "/div/div[@class='postbody' and contains(text(), 'Last edited by')]";
            String timestampPath = "(" + postXpath + ")[" + (i + 1) + "]"; //TODO: Figure out formatter.

            String postBody = ((HtmlDivision)page.getFirstByXPath(contentPath)).asText();

            HtmlAnchor posterUrl = (HtmlAnchor)page.getFirstByXPath(posterPath);
            String     posterName = posterUrl.asText();
            int        posterId = Integer.parseInt(posterUrl.getAttribute("href").replaceAll(".*\\/u", ""));
            ForumUser  poster = ForumUser.getUserFor(posterId, posterName, getContext());
            poster.setName(posterName);

            HtmlAnchor titleLink = (HtmlAnchor)page.getFirstByXPath(titlePath);
            String     title = titleLink.getAttribute("href").split("#")[1];
            int        postId = Integer.parseInt(title);
            //HtmlAnchor timeStamp = (HtmlAnchor)page.getFirstByXPath(timestampPath);


            boolean editable = page.getByXPath(editPath).size() > 0;

            ArrayList<Command> commands = new ArrayList<Command>();
            List<?>            commandElements = page.getByXPath(commandPath);
            List<?>            hiddenCommandElements = page.getByXPath(hiddenCommandPath);

            boolean  edited = page.getByXPath(editedPath).size() > 0;
            DateTime postTime = null; //DateTime.parse(timeStamp.asText(), TIME_FORMAT);

            HalolzPost post = new HalolzPost(this, postId, postTime, poster, commands, editable, postBody);

            //Parse normal commands.
            for (Object e : commandElements) {
                for (DomNode element : ((HtmlStrong)e).getChildren()) {
                    if (!element.getNodeName().equals("#text"))
                        element.remove();
                }
                String command = ((HtmlStrong)e).asText();
                if (command.matches("\\[\\]?.*\\]\\]?"))
                    commands.add(new Command(command, poster, edited, false, post));
            }

            //Parse hidden commands.
            for (Object e : hiddenCommandElements) {
                if (((HtmlSpan)e).getFirstElementChild() != null)
                    continue;
                String command = ((HtmlSpan)e).asText();
                if (command.matches("\\[\\]?.*\\]\\]?"))
                    commands.add(new Command(command, poster, edited, true, post));
            }

            addPost(post);
        }
    }

    @Override
    protected String parseThreadTitle(HtmlPage page) {
        return ((HtmlAnchor)page.getFirstByXPath("//h1[@class='page-title']/a")).asText();
    }

    @Override
    protected boolean isValidThreadPage(HtmlPage page) {
        return page.getByXPath("//p[text()='No posts exist for this topic']").size() == 0;
    }


    @Override
    public ForumContext getContext() {
        return HalolzContext.INSTANCE;
    }
}
