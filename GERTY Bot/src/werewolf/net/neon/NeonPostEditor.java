package werewolf.net.neon;

import java.io.IOException;

import werewolf.net.ForumContext;
import werewolf.net.ForumPostEditor;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextArea;

public class NeonPostEditor implements ForumPostEditor
{
	private static final long	serialVersionUID	= -6341434172839866953L;

	private HtmlInput			subject;
	private HtmlTextArea		body;
	private HtmlInput			reason;
	private HtmlInput			submit;
	private HtmlInput			preview;
	private HtmlInput			lockPost;
	private String				boardId;
	private String				threadId;
	private int					postId;
	private boolean				isInitalized		= false;
	private HtmlPage			page;

	public NeonPostEditor(String boardId, String threadId, int postId) throws IOException
	{
		this.boardId = boardId;
		this.postId = postId;
		this.threadId = threadId;
	}

	@Override
	public void appendText(String newText) throws IOException
	{
		this.checkValidity();
		this.setText(this.getText() + newText);
	}

	public void changePoster() throws IOException
	{
		// TODO: Implement this function.
	}

	private void checkValidity() throws IOException
	{
		if (!this.isInitalized)
			this.initalize();
	}

	@Override
	public ForumContext getContext()
	{
		return NeonContext.INSTANCE;
	}

	@Override
	public String getReason() throws IOException
	{
		this.checkValidity();
		return this.reason.getValueAttribute();
	}

	@Override
	public String getSubject() throws IOException
	{
		this.checkValidity();
		return this.subject.getValueAttribute();
	}

	@Override
	public String getText() throws IOException
	{
		this.checkValidity();
		return this.body.getText();
	}

	private void initalize() throws IOException
	{
		this.page = this.getContext().getPostEditPage(this.boardId, this.threadId, this.postId);

		for (HtmlForm form : this.page.getForms())
		{
			if (!form.getAttribute("name").equals("postform"))
				continue;
			this.subject = form.getInputByName("subject");
			this.body = (HtmlTextArea) form.getElementsByAttribute("textarea", "name", "message").get(0);
			this.reason = form.getInputByName("edit_reason");
			this.submit = form.getInputByName("post");
			this.preview = form.getInputByName("preview");
			this.lockPost = form.getInputByName("lock_post");
			this.isInitalized = true;
			return;
		}
		// TODO: Make this more verbose about what's actually happening.
		throw new IOException("Could not initalize post editor: access denied by remote host.\nURL: " + this.page.getUrl());
	}

	@Override
	public boolean isLocked() throws IOException
	{
		this.checkValidity();
		return this.lockPost.isChecked();
	}

	@Override
	public void setLocked(boolean locked) throws IOException
	{
		this.checkValidity();
		this.lockPost.setChecked(locked);
	}

	@Override
	public void setReason(String newReason) throws IOException
	{
		this.checkValidity();
		this.reason.setValueAttribute(newReason);
	}

	@Override
	public void setSubject(String newSubject) throws IOException
	{
		this.checkValidity();
		this.subject.setValueAttribute(newSubject);
	}

	@Override
	public void setText(String newText) throws IOException
	{
		this.checkValidity();
		this.body.setText(newText);
	}

	@Override
	public void submit() throws IOException
	{
		if (!this.isInitalized)
			return;
		this.getContext().pagePostLock();
		HtmlPage postPage = this.submit.click();
		// System.out.println(postPage.getUrl());
		// System.out.println(postPage.asText());
		if (postPage.getUrl().getRef() == null)
		{
			System.out.println("Retry submission...");
			try
			{
				Thread.sleep(15 * 1000);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			postPage.refresh();
		}
		this.isInitalized = false;
	}
}
