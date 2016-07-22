package werewolf.net.neon;

import java.io.IOException;
import java.util.List;

import org.joda.time.DateTime;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import werewolf.net.Command;
import werewolf.net.ForumContext;
import werewolf.net.ForumPost;
import werewolf.net.ForumUser;

public class NeonPost extends ForumPost
{
	private static final long serialVersionUID = 8585339145492188641L;

	public NeonPost(NeonThread parent, int postId, DateTime postTime, ForumUser poster, List<Command> commands, boolean isEditable, String content)
	{
		super(parent, postId, postTime, poster, commands, isEditable, content);
		if (!poster.getContext().equals(this.getContext()))
			throw new IllegalArgumentException("Invalid poster context.");
	}

	@Override
	public void executeDelete() throws IOException
	{
		HtmlPage deletePage = this.getContext().getPostDeletePage(this.getBoardId(), this.getThreadId(), this.postId);
		String formName = "confirm";
		List<HtmlForm> forms = deletePage.getForms();

		for (HtmlForm form : forms)
		{
			if (!form.getAttribute("name").equals(formName))
				continue;
			deletePage = form.getInputByName("confirm").click();
			if (deletePage.getUrl().getRef() == null)
			{
				System.out.println("Unable to delete post - trying again...");
				try
				{
					Thread.sleep(15 * 1000);
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				deletePage.refresh();
			}
			return;
		}
		throw new IOException("Coult not locate post form to submit.");
	}

	@Override
	public ForumContext getContext()
	{
		return NeonContext.INSTANCE;
	}

	/**
	 * @return A ForumPostEditor object which can be used to edit this post.
	 * @throws IllegalArgumentException
	 *             If the post is not editable.
	 */
	@Override
	public NeonPostEditor getEditor() throws IllegalStateException, IOException
	{
		// if (!isEditable)
		// throw new IllegalStateException("Post is not editable. (id=" + postId
		// + ")");
		return new NeonPostEditor(this.getBoardId(), this.getThreadId(), this.postId);
	}
}
