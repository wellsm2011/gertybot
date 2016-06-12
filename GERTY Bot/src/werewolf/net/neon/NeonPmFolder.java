package werewolf.net.neon;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import com.gargoylesoftware.htmlunit.html.HtmlParagraph;

import java.io.IOException;

import java.util.LinkedList;

import java.util.List;

import org.joda.time.DateTime;

import org.joda.time.format.DateTimeFormat;

import werewolf.net.ForumContext;
import werewolf.net.ForumUser;
import werewolf.net.PmFolder;
import werewolf.net.PrivateMessage;

public class NeonPmFolder extends PmFolder {
    public NeonPmFolder(String folderName) {
        super(folderName);
    }


    @Override
    protected void deleteMessageFromFolder(int id) throws IOException {
        HtmlPage page = getContext().getDeleteMessagePage(id);

        for (HtmlForm form : page.getForms()) {
            if (!form.getAttribute("name").equals("confirm"))
                continue;
            form.getInputByName("confirm").click();
            return;
        }
        throw new IOException("Could not find form to submit.");
    }


    @Override
    protected void readMessage(PrivateMessage msg) throws IOException {
        HtmlPage page = getContext().getReadMessagePage(msg.getId());
        String message = ((HtmlAnchor) page.getByXPath("//td/div[@class='postbody']").get(0)).asText();
        ForumUser from = null;
        LinkedList<ForumUser> to = new LinkedList<>();
        LinkedList<ForumUser> bcc = new LinkedList<>();


        List<?> row4 = page.getByXPath("//td/div[@id='pagecontent']/table/tbody/tr[4]/td/a");
        List<?> row5 = page.getByXPath("//td/div[@id='pagecontent']/table/tbody/tr[5]/td/a");

        if (row5.size() > 0) {
            dumpUsers(row4, to);
            dumpUsers(row5, bcc);
        } else if (true) //TODO: Implement conditional.
            dumpUsers(row4, to);
        else
            dumpUsers(row4, bcc);

        msg.initalize(from, to, bcc, message);
    }

    private void dumpUsers(List<?> rawUsers, LinkedList<ForumUser> users) {
        for (Object obj : rawUsers) {
            HtmlAnchor userLink = (HtmlAnchor) obj;
            int id = Integer.parseInt(userLink.getAttribute("href").replaceAll(".*u=", ""));
            String name = userLink.asText();
            users.add(ForumUser.getUserFor(id, name, getContext()));
        }
    }


    @Override
    protected LinkedList<PrivateMessage> readPage(int start) throws IOException {
        HtmlPage page = getContext().getMessageBoxPage(super.folderName, start);

        String base = "//form[@name='viewfolder']/table/tbody/tr/td/";

        List<?> subjects = page.getByXPath(base + "img");
        List<?> read = page.getByXPath(base + "span[@class='topicauthor']");
        List<?> sent = page.getByXPath(base + "p[@class='topicdetails']");

        int min = Math.min(Math.min(read.size(), sent.size()), subjects.size());

        for (int i = 0; i < min; ++i) {
            //String subject, boolean read, int id, DateTime timestamp, PmFolder folder
            HtmlAnchor subjectLink = (HtmlAnchor) subjects.get(i);
            String url = subjectLink.getAttribute("href");
            int id = Integer.parseInt(url.substring(url.indexOf("p=") + 2));
            DateTime timestamp =
                DateTime.parse(((HtmlParagraph)sent.get(i)).asText(), DateTimeFormat.forPattern("E M d, Y h:mm a"));

            new PrivateMessage(subjectLink.asText(), true, id, timestamp, this);
        }

        return new LinkedList<PrivateMessage>();
    }


    @Override
    public ForumContext getContext() {
        return NeonContext.INSTANCE;
    }

    public static void main(String... cheese) {
        return;
    }
}
