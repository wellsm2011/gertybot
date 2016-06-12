package werewolf.net.halolz;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

import java.io.IOException;

import java.util.List;

import org.joda.time.DateTime;

import werewolf.net.Command;

import werewolf.net.ForumContext;
import werewolf.net.ForumPost;
import werewolf.net.ForumPostEditor;
import werewolf.net.ForumThread;
import werewolf.net.ForumUser;

public class HalolzPost extends ForumPost {
    @SuppressWarnings("compatibility:2296065654835201086")
    private static final long serialVersionUID = -8053924436322728598L;

    public HalolzPost(ForumThread parent, int postId, DateTime postTime, ForumUser poster, List<Command> commands, boolean isEditable,
                      String content) {
        super(parent, postId, postTime, poster, commands, isEditable, content);
    }

    @Override
    public ForumPostEditor getEditor() throws IllegalStateException, IOException {
        if (!isEditable)
            throw new IllegalStateException("Post is not editable. (id=" + postId);
        return new HalolzPostEditor(getBoardId(), getThreadId(), postId);
    }

    @Override
    public void executeDelete() throws IOException {
        HtmlPage deletePage = getContext().getPostDeletePage(getBoardId(), getThreadId(), postId);

        HtmlSubmitInput del = (HtmlSubmitInput)deletePage.getFirstByXPath("//input[@name='confirm' and @value='Yes']");
        if (del == null)
            System.out.println("Unable to delete post.");
        else
            del.click();
    }

    @Override
    public ForumContext getContext() {
        return HalolzContext.INSTANCE;
    }
}
