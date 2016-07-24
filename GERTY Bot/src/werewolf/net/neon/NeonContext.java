package werewolf.net.neon;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import werewolf.Utils;
import werewolf.net.ForumContext;
import werewolf.net.ForumInbox;
import werewolf.net.ForumLogin;
import werewolf.net.ForumMessage;
import werewolf.net.ForumThread;
import werewolf.net.ForumUserDatabase;
import werewolf.net.GameRecord;
import werewolf.net.HostingSignups;
import werewolf.net.PrivateMessage;

public class NeonContext extends ForumContext
{
	private static final Logger	LOGGER				= Logger.getLogger(NeonContext.class.getName());
	private static final long	serialVersionUID	= -4455454156365061005L;

	static
	{
		NeonContext init = null;
		try
		{
			init = new NeonContext();
		} catch (NumberFormatException | IOException e)
		{
			NeonContext.LOGGER.log(Level.SEVERE, "Error initalizing NeonContext", e);
		}
		INSTANCE = init;
	}

	public static final NeonContext INSTANCE;

	public static final String				DOMAIN			= "http://www.neondragon.net/";
	public static final NeonGameRecord		RECORD			= NeonGameRecord.INSTANCE;
	public static final HostingSignups		SIGNUPS			= NeonHostingSignups.INSTANCE;
	public static final int					POLL_INTERVAL	= 2;
	public static final String				RULES_URL		= NeonContext.DOMAIN + "viewtopic.php?f=179&t=15538";
	public static final NeonMessageEncoder	ENCODER			= new NeonMessageEncoder();

	public static final int		WEREWOLF_BOARD		= 178;
	public static final boolean	PARSE_ALL_THREADS	= false;

	public static final NeonInbox INBOX = new NeonInbox("inbox");

	private ForumLogin login;

	private NeonContext() throws NumberFormatException, IOException
	{
		int userId = Integer.parseInt(Utils.getProperty("neonId"));
		String username = Utils.getProperty("neonUsername", "");
		String password = Utils.getProperty("neonPassword", "");
		this.login = new ForumLogin(userId, username, password, this);
	}

	@Override
	public boolean allowExpectedLynch()
	{
		return true;
	}

	@Override
	public boolean checkLogin(final HtmlPage page) throws IOException
	{
		final String username = this.login.getName();
		final String password = this.login.getPassword();
		List<?> sidLink = page.getByXPath("//a[contains(@href, 'sid=')]");

		List<?> loginList = page.getByXPath("//table[@id='maintable']/tbody/tr[2]/td/a[text()='Login']");

		// Don't try to log in unless not currently logged in.
		if (loginList.size() > 0)
		{
			NeonContext.LOGGER.info("Logging into Neon server as " + this.login + "...");
			HtmlAnchor a = (HtmlAnchor) loginList.get(0);
			String url = a.getAttributesMap().get("href").getValue().replaceAll("^\\.", NeonContext.DOMAIN);
			this.pageRequestLock();
			HtmlPage page2 = (HtmlPage) this.CLIENT.getPage(url);

			final HtmlForm form = page2.getForms().get(0);

			form.getInputByName("username").setValueAttribute(username);
			form.getInputByName("password").setValueAttribute(password);
			this.pageRequestLock();
			page2 = form.getInputByName("login").click();
			page2.refresh();
			if (page2.getByXPath("//a[text()='Login']").size() > 0)
				throw new IOException("Could not log into remote server.");
			loginList = page2.getByXPath("//a[contains(text(), 'Logout ')]");
			if (loginList.size() > 0)
			{
				String name = ((HtmlAnchor) loginList.get(0)).asText().replace("Logout ", "");
				NeonContext.LOGGER.info("Logged in as " + name + ".");
			} else
				NeonContext.LOGGER.warning("Could not extract username from page.");
			return false;
		} else if (sidLink.size() > 1)
			// Don't want to click that!
			for (Object obj : sidLink)
			{
			HtmlAnchor link = (HtmlAnchor) obj;
			if (!link.asText().contains("FAQ"))
			continue;
			link.click();
			break;
			}
		return true;
	}

	@Override
	public String getBoardUrl(String boardId)
	{
		return NeonContext.DOMAIN + "viewforum.php?f=" + boardId;
	}

	@Override
	public String getComposeMessageUrl()
	{
		return NeonContext.DOMAIN + "ucp.php?i=pm&mode=compose";
	}

	@Override
	public String getComposeMessageUrl(int messageId)
	{
		return NeonContext.DOMAIN + "ucp.php?i=pm&mode=compose&action=reply&p=" + messageId;
	}

	@Override
	public String getDeleteMessageUrl(int id)
	{
		return NeonContext.DOMAIN + "ucp.php?i=pm&mode=compose&action=delete&p=" + id;
	}

	@Override
	public String getDomain()
	{
		return NeonContext.DOMAIN;
	}

	@Override
	public List<ForumThread> getGameThreads() throws IOException
	{
		LinkedList<ForumThread> output = new LinkedList<ForumThread>();
		List<ForumThread> threads = new NeonBoard(NeonContext.WEREWOLF_BOARD).getThreads();
		for (ForumThread thread : threads)
			if (thread.isStickied() || NeonContext.PARSE_ALL_THREADS)
				output.add(thread);
		return output;
	}

	@Override
	public ForumInbox getInbox()
	{
		return NeonContext.INBOX;
	}

	@Override
	public ForumLogin getLogin()
	{
		return this.login;
	}

	@Override
	public String getMessageBoxUrl(String folder, int page)
	{
		return NeonContext.DOMAIN + "ucp.php?i=pm&folder=" + folder + "&start=" + page * 25;
	}

	@Override
	public List<PrivateMessage> getPMs(int page) throws IOException
	{
		// TODO: Implement PM reading in the bot.
		this.pageRequestLock();

		LinkedList<PrivateMessage> output = new LinkedList<PrivateMessage>();
		HtmlPage initialPage = this.getPage(NeonContext.DOMAIN + "ucp.php?i=pm&start=" + page * 25);
		String baseXPath = "//div[@id='pagecontent']/form/table[last()]/tbody/tr";
		// Iterator<?> repliedTo = initialPage.getByXPath(baseXPath +
		// "/td[1]/img[last()]").iterator();
		Iterator<?> newMessage = initialPage.getByXPath(baseXPath + "/td[1]/img[last()]").iterator();
		Iterator<?> messageLink = initialPage.getByXPath(baseXPath + "/td[2]/span/a[last()]").iterator();
		// Iterator<?> senderLink = initialPage.getByXPath(baseXPath +
		// "/td[3]/p/a[last()]").iterator();
		// Iterator<?> timestamp = initialPage.getByXPath(baseXPath +
		// "/td[1]").iterator();

		while (newMessage.hasNext())
		{
			HtmlAnchor link = (HtmlAnchor) messageLink.next();

			String subject = link.asText();
		}

		return output;
	}

	@Override
	public int getPollInterval()
	{
		return NeonContext.POLL_INTERVAL;
	}

	@Override
	public String getPostDeleteUrl(String boardId, String threadId, int postId)
	{
		return NeonContext.DOMAIN + "posting.php?mode=delete&f=" + boardId + "&p=" + postId;
	}

	@Override
	public String getPostEditUrl(String boardId, String threadId, int postId)
	{
		return NeonContext.DOMAIN + "posting.php?mode=edit&f=" + boardId + "&p=" + postId;
	}

	@Override
	public String getPostUrl(String threadId, int start, int postId)
	{
		return NeonContext.DOMAIN + "viewtopic.php?p=" + postId + "#p" + postId;
	}

	@Override
	public String getReadMessageUrl(int id)
	{
		return NeonContext.DOMAIN + "ucp.php?i=pm&mode=view&p=" + id;
	}

	@Override
	public GameRecord getRecord()
	{
		return NeonContext.RECORD;
	}

	@Override
	public String getRulesUrl()
	{
		return NeonContext.RULES_URL;
	}

	@Override
	public HostingSignups getSignups()
	{
		return NeonContext.SIGNUPS;
	}

	@Override
	public String getThreadReplyUrl(String boardId, String threadId)
	{
		return NeonContext.DOMAIN + "posting.php?mode=reply&f=" + boardId + "&t=" + threadId;
	}

	@Override
	public String getThreadUrl(String boardId, String threadId)
	{
		return NeonContext.DOMAIN + "viewtopic.php?t=" + threadId;
	}

	@Override
	public String getThreadUrl(String boardId, String threadId, int start)
	{
		return NeonContext.DOMAIN + "viewtopic.php?t=" + threadId + "&start=" + (start - 1) * 15;
	}

	@Override
	public ForumUserDatabase getUserDatabase()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUserProfileUrl(int userId)
	{
		return NeonContext.DOMAIN + "memberlist.php?mode=viewprofile&u=" + userId;
	}

	@Override
	public void makePm(HtmlPage pmPage, String[] to, String[] bcc, String subject, ForumMessage body) throws IOException
	{
		String formName = "postform";

		if (to.length == 0 && bcc.length == 0)
			throw new IllegalArgumentException("No users to send to.");

		// Sets BCC before TO. If a user is in both, the TO will override.
		pmPage = this.setPmRecipients(this.setPmRecipients(pmPage, bcc, true), to, false);

		for (HtmlForm form : pmPage.getForms())
		{
			if (!form.getAttribute("name").equals(formName))
				continue;
			form.getInputByName("subject").setValueAttribute(subject);

			HtmlElement message = form.getElementsByAttribute("textarea", "name", "message").get(0);
			message.click();
			message.type(body.formatString(NeonContext.ENCODER));
			this.pagePostLock();
			pmPage = ((HtmlElement) pmPage.getFirstByXPath("//input[@name='post' and @class='btnmain']")).click();
			// TODO: Check to ensure PM was sent successfully.
			return;
		}
		throw new IOException("Coult not locate post form to submit.");
	}

	@Override
	public void makePost(HtmlPage postPage, final ForumMessage body, final String subject) throws IOException
	{
		String formName = "postform";
		List<HtmlForm> forms = postPage.getForms();
		HtmlPage initialPage = postPage;

		for (HtmlForm form : forms)
		{
			if (!form.getAttribute("name").equals(formName))
				continue;
			if (subject.length() > 0)
				form.getInputByName("subject").setValueAttribute(subject);
			HtmlElement message = form.getElementsByAttribute("textarea", "name", "message").get(0);
			message.click();
			message.type(body.formatString(NeonContext.ENCODER));

			this.pagePostLock();
			postPage = form.getInputByName("post").click();
			if (postPage.getUrl().getRef() == null)
			{
				NeonContext.LOGGER.warning("NeonContext: Unable to post - trying again...\nURL: " + initialPage.getUrl().toString());
				try
				{
					Thread.sleep(30 * 1000);
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				this.checkLogin(postPage);
				try
				{
					Thread.sleep(30 * 1000);
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				this.makePost((HtmlPage) initialPage.refresh(), body, subject);
			}
			return;
		}
		throw new IOException("Coult not locate post form to submit.");
	}

	private HtmlPage setPmRecipients(HtmlPage pmPage, String[] users, boolean bcc) throws IOException
	{
		if (users.length == 0)
			return pmPage;
		HtmlElement usernames = (HtmlElement) pmPage.getFirstByXPath("//textarea[@name='username_list']");
		usernames.click();
		for (String user : users)
			usernames.type(user + "\n");
		return ((HtmlElement) pmPage.getFirstByXPath("//input[@name='add_" + (bcc ? "bcc" : "to") + "']")).click();
	}

	@Override
	public String toString()
	{
		return "NeonContext";
	}
}
