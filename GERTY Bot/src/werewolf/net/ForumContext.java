package werewolf.net;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.IncorrectnessListener;
import com.gargoylesoftware.htmlunit.InteractivePage;
import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HTMLParserListener;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptErrorListener;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.logging.Level;

import org.apache.commons.logging.LogFactory;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.ErrorHandler;

import werewolf.game.WerewolfGame;

public abstract class ForumContext implements Serializable, Runnable {
	@SuppressWarnings("compatibility:-8891234118977472905")
	private static final long serialVersionUID = -6670513047683798183L;

	private static class ThreadPriority implements Comparable<ThreadPriority> {
		private final ThreadManager manager;
		private double priority = 20;

		private ThreadPriority(ThreadManager manager) {
			this.manager = manager;
		}

		private void increment(int mean) {
			priority += (Math.random() * mean) + mean / 2.0;
		}

		@Override
		public int compareTo(ThreadPriority o) {
			return (int) Math.signum(priority - o.priority);
		}
	}

	public final WebClient CLIENT = new WebClient(BrowserVersion.FIREFOX_38);
	public final ForumLogin LOGIN_USER;
	public final String DOMAIN;
	public final ForumUserDatabase USERS = new ForumUserDatabase(this);
	public final GameRecord RECORD;
	public final transient HostingSignups SIGNUPS;
	public final int POLL_INTERVAL;
	public final double REQUEST_INTERVAL = 2; // Seconds between HTML page
												// requests.
	public final double POST_INTERVAL = 60; // Seconds between posts and PM
											// submissions.
	public final String RULES_URL;
	public final PmManager PM_MANAGER;

	private long lastPageRequest = System.currentTimeMillis()
			- (long) (REQUEST_INTERVAL * 500);
	private long lastPostSubmission = System.currentTimeMillis()
			- (long) (POST_INTERVAL * 500);

	public ForumContext(int u, String username, String password, String domain,
			GameRecord record, HostingSignups signups, PmManager pmManager,
			int pollInterval, String rulesUrl) {
		LOGIN_USER = new ForumLogin(u, username, password, this);
		DOMAIN = domain;
		RECORD = record;
		SIGNUPS = signups;
		POLL_INTERVAL = pollInterval;
		RULES_URL = rulesUrl;
		PM_MANAGER = pmManager;
		Utils.CONTEXTS.add(this);
		disableLogging();
		System.out.println("Context Created:\n      User=" + LOGIN_USER
				+ "\n      Domain=" + DOMAIN);
	}

	/**
	 * @param page
	 *            The forum page to check login on.
	 * @return True if the client is already logged in or false if a login has
	 *         been made.
	 * @throws IOException
	 *             If the client could not log in.
	 */
	protected abstract boolean checkLogin(final HtmlPage page)
			throws IOException;

	/**
	 * @return A list of forum threads that contain a game.
	 * @throws IOException
	 */
	public abstract List<ForumThread> getGameThreads() throws IOException;

	/**
	 * Makes a new post with the default subject (Re: topic subject).
	 *
	 * @param postPage
	 *            The post page.
	 * @param body
	 *            The message to post.
	 * @throws IOException
	 *             If the given page is not a forum post page or if any of the
	 *             underlying network calls throw an error.
	 */
	public void makePost(HtmlPage postPage, String body) throws IOException {
		makePost(postPage, body, "");
	}

	/**
	 * @param postPage
	 *            The post page.
	 * @param body
	 *            The message to post.
	 * @param subject
	 *            The subject of the message.
	 * @throws IOException
	 *             If the given page is not a forum post page or if any of the
	 *             underlying network calls throw an error.
	 */
	public abstract void makePost(HtmlPage postPage, final String body,
			final String subject) throws IOException;

	/**
	 *
	 * @param page
	 *            The page number to load.
	 * @return A list of the private messages on the given page.
	 * @throws IOException
	 *             If any of the underlying network calls throw an error.
	 */
	public abstract List<PrivateMessage> getPMs(int page) throws IOException;

	/**
	 * Sends a new PM.
	 *
	 * @param to
	 *            A list of users to send the message to.
	 * @param bcc
	 *            A list of users to blind copy on the message.
	 * @param body
	 *            The message to send.
	 * @param subject
	 *            The subject of the message.
	 * @throws IOException
	 *             If any of the underlying network calls throw an error.
	 */
	public void makePm(String[] to, String[] bcc, String subject, String body)
			throws IOException {
		makePm(getComposeMessagePage(), to, bcc, subject, body);
	}

	/**
	 * Sends a new PM.
	 *
	 * @param page
	 *            The loaded page with the unfinished message.
	 * @param to
	 *            A list of users to send the message to.
	 * @param bcc
	 *            A list of users to blind copy on the message.
	 * @param body
	 *            The message to send.
	 * @param subject
	 *            The subject of the message.
	 * @throws IOException
	 *             If any of the underlying network calls throw an error.
	 */
	public abstract void makePm(HtmlPage page, String[] to, String[] bcc,
			String subject, String body) throws IOException;

	/**
	 * Called when a new page request is needed from the server. Pauses the
	 * thread until the timeout for the last page request is finished.
	 */
	public void pageRequestLock() {
		checkLock((long) (REQUEST_INTERVAL * 1000) + lastPageRequest);
		lastPageRequest = System.currentTimeMillis();
	}

	/**
	 * Called when a post must be made (be it a post to a thread, a PM, an edit,
	 * etc) to the forum. Pauses the thread until the timeout for the last post
	 * request is finished.
	 */
	public void pagePostLock() {
		pageRequestLock();
		checkLock((long) (POST_INTERVAL * 1000) + lastPostSubmission);
		lastPostSubmission = System.currentTimeMillis();
	}

	private void checkLock(long nextUnlock) {
		long currentTime = System.currentTimeMillis();
		long waitTime = nextUnlock - currentTime;
		try {
			if (waitTime > 0) {
				synchronized (this) {
					this.wait(waitTime);
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns an HtmlPage object for a given URL.
	 *
	 * @param url
	 * @return
	 * @throws IOException
	 */
	protected HtmlPage getPage(String url) throws IOException {
		pageRequestLock();
		HtmlPage page = (HtmlPage) CLIENT.getPage(url);
		if (!checkLogin(page))
			page = (HtmlPage) CLIENT.getPage(url);
		if (!page.getUrl().toString().equalsIgnoreCase(url))
			throw new IOException("Unknown page redirect from " + url + " to "
					+ page.getUrl());
		return page;
	}

	/**
	 * Returns an HtmlPage for the first page of threads in a given forum board.
	 *
	 * @param boardId
	 * @return
	 * @throws IOException
	 */
	public HtmlPage getBoardPage(String boardId) throws IOException {
		pageRequestLock();
		return getPage(getBoardUrl(boardId));
	}

	/**
	 * Returns the URL for the first page of threads in a given forum board.
	 *
	 * @param boardId
	 * @return
	 */
	public abstract String getBoardUrl(String boardId);

	/**
	 * Returns an HtmlPage for the first page of a given forum thread.
	 *
	 * @param boardId
	 * @param threadId
	 * @return
	 * @throws IOException
	 */
	public HtmlPage getThreadPage(String boardId, String threadId)
			throws IOException {
		pageRequestLock();
		return getPage(getThreadUrl(boardId, threadId));
	}

	public abstract String getThreadUrl(String boardId, String threadId);

	/**
	 * Returns an HtmlPage for a given page of a given forum thread.
	 *
	 * @param boardId
	 * @param threadId
	 * @param start
	 * @return
	 * @throws IOException
	 */
	public HtmlPage getThreadPage(String boardId, String threadId, int start)
			throws IOException {
		pageRequestLock();
		return getPage(getThreadUrl(boardId, threadId, start));
	}

	public abstract String getThreadUrl(String boardId, String threadId,
			int start);

	/**
	 * Returns an HtmlPage to reply to a given thread.
	 *
	 * @param boardId
	 * @param threadId
	 * @return
	 * @throws IOException
	 */
	public HtmlPage getThreadReplyPage(String boardId, String threadId)
			throws IOException {
		pageRequestLock();
		return getPage(getThreadReplyUrl(boardId, threadId));
	}

	public abstract String getThreadReplyUrl(String boardId, String threadId);

	/**
	 * Returns an HtmlPage to delete a given post.
	 *
	 * @param boardId
	 * @param threadId
	 * @param postId
	 * @return
	 * @throws IOException
	 */
	public HtmlPage getPostDeletePage(String boardId, String threadId,
			int postId) throws IOException {
		pageRequestLock();
		return getPage(getPostDeleteUrl(boardId, threadId, postId));
	}

	public abstract String getPostDeleteUrl(String boardId, String threadId,
			int postId);

	/**
	 * Returns an HtmlPage to edit a given post.
	 *
	 * @param boardId
	 * @param threadId
	 * @param postId
	 * @return
	 * @throws IOException
	 */
	public HtmlPage getPostEditPage(String boardId, String threadId, int postId)
			throws IOException {
		pageRequestLock();
		return getPage(getPostEditUrl(boardId, threadId, postId));
	}

	public abstract String getPostEditUrl(String boardId, String threadId,
			int postId);

	/**
	 * Gets a new compose message page that replies to a given message.
	 *
	 * @param messageId
	 * @return
	 * @throws IOException
	 */
	public HtmlPage getComposeMessagePage(int messageId) throws IOException {
		pageRequestLock();
		return getPage(getComposeMessageUrl(messageId));
	}

	public abstract String getComposeMessageUrl(int messageId);

	/**
	 * Returns an HtmlPage to compose a new PM.
	 * 
	 * @return
	 * @throws IOException
	 */
	public HtmlPage getComposeMessagePage() throws IOException {
		pageRequestLock();
		return getPage(getComposeMessageUrl());
	}

	public abstract String getComposeMessageUrl();

	/**
	 * Returns an HtmlPage for a given message.
	 * 
	 * @param id
	 * @return
	 * @throws IOException
	 */
	public HtmlPage getReadMessagePage(int id) throws IOException {
		pageRequestLock();
		return getPage(getReadMessageUrl(id));
	}

	public abstract String getReadMessageUrl(int id);

	/**
	 * Returns an HtmlPage for deleting a private message.
	 * 
	 * @param id
	 * @return
	 * @throws IOException
	 */
	public HtmlPage getDeleteMessagePage(int id) throws IOException {
		pageRequestLock();
		return getPage(getDeleteMessageUrl(id));
	}

	public abstract String getDeleteMessageUrl(int id);

	public abstract String getPostUrl(String threadId, int page, int postId);

	/**
	 * Returns an HtmlPage for a given user's profile.
	 * 
	 * @param userId
	 * @return
	 * @throws IOException
	 */
	public HtmlPage getMessageBoxPage(String folder, int page)
			throws IOException {
		pageRequestLock();
		return getPage(getMessageBoxUrl(folder, page));
	}

	public abstract String getMessageBoxUrl(String folder, int page);

	/**
	 * Returns an HtmlPage for a given user's profile.
	 * 
	 * @param userId
	 * @return
	 * @throws IOException
	 */
	public HtmlPage getUserProfilePage(int userId) throws IOException {
		pageRequestLock();
		return getPage(getUserProfileUrl(userId));
	}

	public abstract String getUserProfileUrl(int userId);

	@Override
	public String toString() {
		return DOMAIN;
	}

	public String getCountdownUrl(int round, boolean isEst, int year,
			int month, int day, int hour) {
		String p0 = "";
		if (isEst)
			p0 = "&p0=179";
		return "http://www.timeanddate.com/countdown/generic?msg=R" + round
				+ "%20Countdown" + p0 + "&year=" + year + "&month=" + month
				+ "&day=" + day + "&hour=" + hour + "&min=0&sec=0";
	}

	/**
	 * Inspects games and sets up WerewolfGame objects for new games. NOTE: The
	 * given PriorityQueue is modified by this method.
	 *
	 * @param games
	 * @param threads
	 * @throws IOException
	 */
	private static void checkGames(PriorityQueue<ThreadPriority> games,
			List<? extends ForumThread> threads) throws IOException {
		Iterator<ThreadPriority> iter = games.iterator();
		LinkedList<ForumUser> hosts = new LinkedList<ForumUser>();

		while (iter.hasNext()) {
			ThreadManager manager = iter.next().manager;
			if (!threads.contains(manager.getThread())) {
				iter.remove();
			} else if (manager instanceof WerewolfGame) {
				WerewolfGame game = (WerewolfGame) manager;
				hosts.add(game.getHost());
				ForumUser cohost = game.getCohost();
				if (cohost != null)
					hosts.add(cohost);
			}
		}

		for (ForumThread thread : threads) {
			boolean found = false;
			for (ThreadPriority wrapper : games) {
				if (wrapper.manager.getThread().equals(thread)) {
					found = true;
					break;
				}
			}
			if (!found) {
				WerewolfGame game = new WerewolfGame(thread);
				hosts.add(game.getHost());
				ForumUser cohost = game.getCohost();
				if (cohost != null)
					hosts.add(cohost);
				games.add(new ThreadPriority(game));
			}
		}
	}

	public void disableLogging() {
		LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log",
				"org.apache.commons.logging.impl.NoOpLog");

		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(
				Level.OFF);
		java.util.logging.Logger.getLogger("org.apache.commons.httpclient")
				.setLevel(Level.OFF);

		CLIENT.setIncorrectnessListener(new IncorrectnessListener() {

			@Override
			public void notify(String arg0, Object arg1) {

			}
		});
		CLIENT.setCssErrorHandler(new ErrorHandler() {

			@Override
			public void warning(CSSParseException exception)
					throws CSSException {

			}

			@Override
			public void fatalError(CSSParseException exception)
					throws CSSException {
				exception.printStackTrace();
			}

			@Override
			public void error(CSSParseException exception) throws CSSException {

			}
		});
		CLIENT.setJavaScriptErrorListener(new JavaScriptErrorListener() {
			@Override
			public void scriptException(InteractivePage page,
					ScriptException scriptException) {

			}

			@Override
			public void timeoutError(InteractivePage page, long allowedTime,
					long executionTime) {

			}

			@Override
			public void malformedScriptURL(InteractivePage page, String url,
					MalformedURLException malformedURLException) {

			}

			@Override
			public void loadScriptError(InteractivePage page, URL scriptUrl,
					Exception exception) {

			}
		});
		CLIENT.setHTMLParserListener(new HTMLParserListener() {
			@Override
			public void error(String string, URL url, String string1, int i,
					int i1, String string2) {
			}

			@Override
			public void warning(String string, URL url, String string1, int i,
					int i1, String string2) {
			}
		});

		CLIENT.getOptions().setThrowExceptionOnFailingStatusCode(false);
		CLIENT.getOptions().setThrowExceptionOnScriptError(false);
	}

	public void run() {
		while (true) {
			try {
				PriorityQueue<ThreadPriority> games = new PriorityQueue<ThreadPriority>();
				games.add(new ThreadPriority(null));

				while (true) {
					try {
						long initTime = System.currentTimeMillis();

						ThreadPriority wrapper = games.poll();
						wrapper.increment(games.size());
						for (ThreadPriority i : games)
							i.priority -= 1;

						if (wrapper.manager == null) {
							checkGames(games, getGameThreads());
							wrapper.increment(games.size() * 10);
						} else {
							if (!wrapper.manager.update())
								wrapper.increment(games.size());
							if (!wrapper.manager.getThread().isStickied())
								wrapper.increment(games.size() * 4);
						}
						games.add(wrapper);

						long sleepTime = POLL_INTERVAL * 60 * 1000
								- (System.currentTimeMillis() - initTime);
						if (sleepTime > 0)
							Thread.sleep(sleepTime);
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public abstract String header(String text);

	public abstract String spoiler(String title, String text);

	public abstract boolean allowPMs();

	public abstract boolean allowExpectedLynch();

	public abstract String strike(String text);

	public boolean equals(ForumContext o) {
		return o.toString().equals(toString());
	}
}
