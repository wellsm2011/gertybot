package werewolf.net.halolz;

import com.gargoylesoftware.htmlunit.ThreadedRefreshHandler;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import werewolf.net.ForumContext;
import werewolf.net.ForumThread;
import werewolf.net.GameRecord;
import werewolf.net.HostingSignups;
import werewolf.net.PmFolder;
import werewolf.net.PmManager;
import werewolf.net.PrivateMessage;
import werewolf.net.neon.NeonHostingSignups;

public class HalolzContext extends ForumContext {
    @SuppressWarnings("compatibility:-6749484763766019271")
    private static final long serialVersionUID = -5637847612329034786L;


    public static final HalolzContext INSTANCE = new HalolzContext();

    public static final String         DOMAIN = "http://halolzmafia.iftopic.com/";
    public static final GameRecord     RECORD = HalolzGameRecord.INSTANCE;
    public static final HostingSignups SIGNUPS = null;
    public static final int            POLL_INTERVAL = 5;
    public static final String         RULES_URL = DOMAIN + "t2-mafia-ruleset-version-4beta";
    public static final PmManager      PM_MANAGER = setupPmManager();

    public static final String WEREWOLF_BOARD = "f2-mafia-games";

    private HalolzContext() {
        super(48, "VoteBot 9000", "rainknowsthefacts!", DOMAIN, RECORD, SIGNUPS, PM_MANAGER, POLL_INTERVAL, RULES_URL);
        CLIENT.getOptions().setJavaScriptEnabled(false);
        CLIENT.setRefreshHandler(new ThreadedRefreshHandler());
    }

    @Override
    public boolean checkLogin(HtmlPage page) throws IOException {
        final String username = LOGIN_USER.getName();
        final String password = LOGIN_USER.getPassword();
        try {
            if (page.getElementById("logout").asText().length() > 0)
                return true;
        } catch (NullPointerException e) {
            System.out.println("Logging into Halolz server as " + LOGIN_USER + "...");
        }

        HtmlPage page2 = checkPage((HtmlPage)CLIENT.getPage(DOMAIN + "login"));
        HtmlForm form;

        try {
            form = page2.getForms().get(1);
        } catch (IndexOutOfBoundsException ex) {
            System.err.println("Unable to access Halolz server. Waiting 30 seconds...");
            synchronized (Thread.currentThread()) {
                try {
                    Thread.currentThread().wait(15 * 1000);
                    CLIENT.getPage(DOMAIN);
                    Thread.currentThread().wait(15 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return checkLogin(page);
            }
        }

        form.getInputByName("username").setValueAttribute(username);
        form.getInputByName("password").setValueAttribute(password);
        page2 = form.getInputByName("login").click();
        try {
            System.out.println("Logged in as " + page2.getElementById("logout").asText().replace("Log out ", ""));
        } catch (NullPointerException e) {
            throw new IOException("Could not log into remote server.");
        }
        return false;
    }

    private HtmlPage checkPage(HtmlPage page) throws IOException {
        String errStr =
            "Request limit exceeded\n" + "Request limit exceeded\n" +
            "It appears that your computer has made too many requests on the same page recently. " +
            "Please make sure your antivirus is up to date.\n" + "\n" +
            "If you have any questions about this message, feel free to contact the support";

        if (page.asText().equals(errStr)) {
            System.err.println("Unable to access Halolz server. Waiting 30 seconds...");
            synchronized (Thread.currentThread()) {
                try {
                    CLIENT.getPage(DOMAIN);
                    Thread.currentThread().wait(30 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return checkPage((HtmlPage)page.refresh());
            }
        }

        return page;
    }

    @Override
    public List<ForumThread> getGameThreads() throws IOException {
        LinkedList<ForumThread> unlockedThreads = new LinkedList<ForumThread>();

        HalolzBoard board = new HalolzBoard(WEREWOLF_BOARD);

        for (ForumThread thread : board.getThreads()) {
            if (!thread.isLocked())
                unlockedThreads.add(thread);
        }

        return unlockedThreads;
    }

    @Override
    public void makePost(HtmlPage postPage, String body, String subject) throws IOException {
        HtmlForm form = postPage.getForms().get(1);

        if (subject.length() > 0)
            form.getInputByName("subject").setValueAttribute(subject);
        HtmlElement message = form.getElementsByAttribute("textarea", "name", "message").get(0);
        message.click();
        message.type(body);
        postPage = form.getInputByName("post").click();
    }

    @Override
    public List<PrivateMessage> getPMs(int page) throws IOException {
        return Collections.emptyList();
    }

    @Override
    public void makePm(HtmlPage pmPage, String[] to, String[] bcc, String body, String subject) throws IOException {}

    @Override
    public String getBoardUrl(String boardId) {
        return DOMAIN + boardId;
    }

    @Override
    public String getThreadUrl(String boardId, String threadId) {
        return DOMAIN + threadId;
    }

    @Override
    public String getThreadUrl(String boardId, String threadId, int start) {
        if (start <= 1)
            return getThreadUrl(boardId, threadId);
        String replacer = threadId.split("-")[0];
        return DOMAIN + threadId.replace(replacer, replacer + "p" + ((start - 1) * 15));
    }

    @Override
    public String getThreadReplyUrl(String boardId, String threadId) {
        return DOMAIN + "post?t=" + threadId.split("-")[0].replace("t", "") + "&mode=reply";
    }

    @Override
    public String getPostDeleteUrl(String boardId, String threadId, int postId) {
        return DOMAIN + "post?p=" + postId + "&mode=delete";
    }

    @Override
    public String getPostEditUrl(String boardId, String threadId, int postId) {
        return DOMAIN + "post?p=" + postId + "&mode=editpost";
    }

    @Override
    public String getComposeMessageUrl(int userId) {
        return DOMAIN + "privmsg?mode=post&u=" + userId;
    }

    @Override
    public String getComposeMessageUrl() {
        return DOMAIN + "privmsg?mode=post";
    }


    @Override
    public String getReadMessageUrl(int id) {
        return DOMAIN + "privmsg?folder=inbox&mode=read&p=" + id;
    }


    @Override
    public String getDeleteMessageUrl(int id) {
        return null;    // No URL to delete messages on Halolz.
    }
    
    @Override
    public HtmlPage getDeleteMessagePage(int id) {
        return null;    //TODO: Implement delete message page.
    }

    @Override
    public String getPostUrl(String threadId, int start, int postId) {
        return getThreadUrl("", threadId, start) + "#" + postId;
    }

    @Override
    public String getUserProfileUrl(int userId) {
        return DOMAIN + "u" + userId;
    }

    @Override
    public String toString() {
        return "HalolzContext";
    }

    @Override
    public String header(String text) {
        return "[size=20][b]" + text + "[/b][/size]";
    }

    @Override
    public String spoiler(String title, String text) {
        return "[spoiler=" + title + "]" + text + "[/spoiler]";
    }

    @Override
    public String strike(String text) {
        return "[strike]" + text + "[/strike]";
    }

    @Override
    public boolean allowPMs() {
        return false;
    }

    @Override
    public boolean allowExpectedLynch() {
        return false;
    }
    
    private static PmManager setupPmManager() {
        HashMap<String, PmFolder> folders = new HashMap<String, PmFolder>();
        return new PmManager(folders);
    }

    @Override
    public String getMessageBoxUrl(String folder, int page) {
        return null;
    }
}
