package werewolf.net.neon;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import com.gargoylesoftware.htmlunit.html.HtmlTextArea;

import java.io.IOException;

import werewolf.net.ForumContext;
import werewolf.net.ForumPostEditor;

public class NeonPostEditor implements ForumPostEditor {
    @SuppressWarnings("compatibility:-6151622093449234334")
    private static final long serialVersionUID = -6341434172839866953L;

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

    public NeonPostEditor(String boardId, String threadId, int postId) throws IOException {
        this.boardId = boardId;
        this.postId = postId;
        this.threadId = threadId;
    }

    private void initalize() throws IOException {
        page = getContext().getPostEditPage(boardId, threadId, postId);

        for (HtmlForm form : page.getForms()) {
            if (!form.getAttribute("name").equals("postform"))
                continue;
            subject = form.getInputByName("subject");
            body = (HtmlTextArea)form.getElementsByAttribute("textarea", "name", "message").get(0);
            reason = form.getInputByName("edit_reason");
            submit = form.getInputByName("post");
            preview = form.getInputByName("preview");
            lockPost = form.getInputByName("lock_post");
            isInitalized = true;
            return;
        }
        //TODO: Make this more verbose about what's actually happening.
        throw new IOException("Could not initalize post editor: access denied by remote host.\nURL: " + page.getUrl());
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
        checkValidity();
        return lockPost.isChecked();
    }


    @Override
    public void setLocked(boolean locked) throws IOException {
        checkValidity();
        lockPost.setChecked(locked);
    }


    @Override
    public void submit() throws IOException {
        if (!isInitalized)
            return;
        getContext().pagePostLock();
        HtmlPage postPage = submit.click();
//        System.out.println(postPage.getUrl());
//        System.out.println(postPage.asText());
        if (postPage.getUrl().getRef() == null) {
            System.out.println("Retry submission...");
            try {
                Thread.sleep(15 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();    
            }
            postPage.refresh();
        }
        isInitalized = false;
    }


    private void checkValidity() throws IOException {
        if (!isInitalized)
            initalize();
    }
    
    
    public void changePoster() throws IOException {
        //TODO: Implement this function.
    }

    @Override
    public ForumContext getContext() {
        return NeonContext.INSTANCE;
    }
}
