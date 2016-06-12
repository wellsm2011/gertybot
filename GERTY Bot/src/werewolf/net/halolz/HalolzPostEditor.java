package werewolf.net.halolz;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextArea;

import java.io.IOException;

import werewolf.net.ForumContext;
import werewolf.net.ForumPostEditor;

public class HalolzPostEditor implements ForumPostEditor {
    @SuppressWarnings("compatibility:489417497458713877")
    private static final long serialVersionUID = -1531634583061381906L;

    private HtmlInput    subject;
    private HtmlTextArea body;
    private HtmlInput    reason;
    private HtmlInput    submit;
    private HtmlInput    preview;
    private HtmlInput    lockPost;
    private String       boardId;
    private String       threadId;
    private int          postId;
    private boolean      isInitalized = false;
    private HtmlPage     page;

    public HalolzPostEditor(String boardId, String threadId, int postId) throws IOException {
        this.boardId = boardId;
        this.postId = postId;
        this.threadId = threadId;
    }

    private void initalize() throws IOException {
        page = getContext().getPostEditPage(boardId, threadId, postId);

        for (HtmlForm form : page.getForms()) {
            if (!form.getAttribute("name").equals("post"))
                continue;
            subject = form.getInputByName("subject");
            body = (HtmlTextArea)page.getElementById("text_editor_textarea");
            reason = form.getInputByName("edit_reason");
            submit = form.getInputByName("post");
            preview = form.getInputByName("preview");
            lockPost = null;
            isInitalized = true;
            return;
        }

        throw new IOException("Could not find post form.");
    }

    @Override
    public void setText(String newText) throws IOException {
        checkValidity();
        body.setText(newText);
    }


    @Override
    public void appendText(String newText) throws IOException {
        checkValidity();
        setText(getText() + newText);
    }


    @Override
    public String getText() throws IOException {
        checkValidity();
        return body.getText();
    }


    @Override
    public void setSubject(String newSubject) throws IOException {
        checkValidity();
        subject.setValueAttribute(newSubject);
    }


    @Override
    public String getSubject() throws IOException {
        checkValidity();
        return subject.getValueAttribute();
    }


    @Override
    public void setReason(String newReason) throws IOException {
        checkValidity();
        reason.setValueAttribute(newReason);
    }


    @Override
    public String getReason() throws IOException {
        checkValidity();
        return reason.getValueAttribute();
    }


    @Override
    public boolean isLocked() throws IOException {
        //Halolz doesn't allow mods to lock individual posts.
        return false;
    }


    @Override
    public void setLocked(boolean locked) throws IOException {
        //Halolz doesn't allow mods to lock individual posts.
        return;
    }


    @Override
    public void submit() throws IOException {
        if (!isInitalized)
            return;
        submit.click();
        isInitalized = false;
    }


    private void checkValidity() throws IOException {
        if (!isInitalized)
            initalize();
    }

    @Override
    public ForumContext getContext() {
        return HalolzContext.INSTANCE;
    }
}
