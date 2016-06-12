package werewolf.net;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;
import java.io.Serializable;

import java.util.LinkedList;
import java.util.List;


public abstract class ForumThread implements Serializable {
    @SuppressWarnings("compatibility:613528308914302850")
    private static final long serialVersionUID = -1204239372191855699L;


    protected LinkedList<ForumPost> posts = new LinkedList<ForumPost>();
    protected LinkedList<ForumPost> deleted = new LinkedList<ForumPost>();
    protected LinkedList<ForumPost> edited = new LinkedList<ForumPost>();

    private boolean stickied = false;
    private boolean locked = false;
    private boolean editable = false;
    private String  threadId;
    private String  boardId;
    private String  title = null;

    private boolean initalized = false;
    private int     postReadIndex = 0;
    private int     pagesParsed = 0;


    protected ForumThread(String boardId, String threadId) {
        this.boardId = boardId;
        this.threadId = threadId;
    }

    protected ForumThread(String boardId, String threadId, String title) {
        this(boardId, threadId);
        this.title = title;
    }

    protected ForumThread(String boardId, String threadId, boolean stickied, boolean locked, boolean editable) {
        this(boardId, threadId);
        this.stickied = stickied;
        this.locked = locked;
        this.editable = editable;
    }

    protected ForumThread(String boardId, String threadId, String title, boolean stickied, boolean locked,
                       boolean editable) {
        this(boardId, threadId, stickied, locked, editable);
        this.title = title;
    }

    public boolean isStickied() {
        return stickied;
    }

    public void setStickied(boolean stickied) {
        this.stickied = stickied;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public String getTitle() throws IOException {
        if (title == null)
            refreshAll();
        return title;
    }

    public String getUrl() throws IOException {
        return getContext().getThreadUrl(boardId, threadId);
    }

    public List<ForumPost> getPosts() throws IOException {
        if (!initalized)
            refreshAll();
        return posts;
    }

    public String getThreadId() {
        return threadId;
    }

    public String getBoardId() {
        return boardId;
    }

    public void post(String message) throws IOException {
        getContext().makePost(getContext().getThreadReplyPage(boardId, threadId), message);
    }

    public void reset() throws IOException {
        refreshAll();
        postReadIndex = 0;
    }

    public void refresh() throws IOException {
        if (!initalized) {
            if (title != null && title.length() > 0)
                System.out.println("Loading new thread (context=" + getContext() + "): " + title);
            else
                System.out.println("Loading new thread (context=" + getContext() + ")...");
        } else
            System.out.println("Reloading Thread (context=" + getContext() + "): " + title);
        initalized = true;

        try {
            HtmlPage page = getContext().getThreadPage(boardId, threadId, pagesParsed);
            title = parseThreadTitle(page);

            do {
                parsePage(page);
                getContext().CLIENT.closeAllWindows();
                page = getContext().getThreadPage(boardId, threadId, ++pagesParsed);
            } while (isValidThreadPage(page));

            pagesParsed -= 1;
            getContext().CLIENT.closeAllWindows();
        } catch (Exception ex) {
            initalized = false;
            throw ex;
        }

        int commands = 0;
        for (ForumPost post : posts)
            commands += post.getCommands().size();

        System.out.println("Thread loaded (context=" + getContext() + "): " + title + ". " + posts.size() +
                           " posts and " + commands + " commands found.");
    }

    public void refreshAll() throws IOException {
        pagesParsed = 1;
        refresh();
    }

    protected abstract void parsePage(HtmlPage page);

    protected abstract String parseThreadTitle(HtmlPage page);

    protected abstract boolean isValidThreadPage(HtmlPage page);

    protected void addPost(ForumPost post) {
        int index = getPostIndex(post);
        if (index >= 0) {
            if (!post.equalCommands(posts.get(index)))
                this.edited.add(post);
            posts.set(index, post);
        } else
            posts.add(post);
    }

    public int getPostIndex(ForumPost post) {
        try {
            if (!initalized)
                refreshAll();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < posts.size(); ++i) {
            if (posts.get(i).equals(post))
                return i;
        }
        return -1;
    }

    public ForumPost nextPost() throws IOException {
        if (!initalized)
            refreshAll();
        if (postReadIndex >= posts.size())
            return null;
        return posts.get(postReadIndex++);
    }

    public int pages() throws IOException {
        if (!initalized)
            refreshAll();
        return (int)Math.ceil(posts.size() / 15.0);
    }

    public abstract ForumContext getContext();

    public boolean equals(ForumThread o) {
        return getThreadId().equals(o.getThreadId()) && getContext().equals(o.getContext());
    }

    @Override
    public String toString() {
        String status = "";
        if (stickied)
            status += ", stickied=true";
        if (locked)
            status += ", locked=true";

        if (title.length() > 0)
            return "[id=" + threadId + ", title=" + title + status + "]";
        return "[id=" + threadId + ", title=unparsed]";
    }
}
