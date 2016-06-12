package werewolf.net;

import java.io.IOException;
import java.io.Serializable;

import java.util.List;

import org.joda.time.DateTime;


public abstract class ForumPost implements Serializable {
    @SuppressWarnings("compatibility:6617185474224489824")
    private static final long serialVersionUID = 1043955423819856116L;

    protected ForumUser     poster;
    protected boolean       isEditable;
    protected boolean       edited = false;
    protected boolean       deleted = false;
    protected List<Command> commands;
    protected int           postId;
    protected String        content;
    protected ForumThread   thread;
    protected DateTime      postTime;


    public ForumPost(ForumThread parent, int postId, DateTime postTime, ForumUser poster, List<Command> commands,
                     boolean isEditable, String content) {
        this.poster = poster;
        this.thread = parent;
        this.commands = commands;
        this.postId = postId;
        this.isEditable = isEditable;
        this.content = content;
        this.postTime = postTime;
    }


    /**
     *
     * @return True if the bot is able to edit this post.
     */
    public boolean isEditable() {
        return isEditable;
    }


    /**
     * @return The ForumUser who created this post.
     */
    public ForumUser getPoster() {
        return poster;
    }
    
    
    /**
     * @return The time this post was created.
     */
    public DateTime getPostTime() {
        return postTime;
    }


    /**
     * @return The ForumThread which this post belongs to.
     */
    public ForumThread getThread() {
        return thread;
    }
    
    
    public String getRawText() {
        return content;
    }


    /**
     * @return The page in the thread where this post is.
     */
    public int getPage() {
        return (int)Math.ceil((thread.getPostIndex(this) + 1) / 15.0);
    }

    /**
     * @return
     * @throws IllegalStateException
     * @throws IOException
     */
    public abstract ForumPostEditor getEditor() throws IllegalStateException, IOException;

    public void delete() throws IOException {
        if (deleted)
            return;
        executeDelete();
        deleted = true;
    }

    public void externalDelete() {
        deleted = true;
    }

    public void externalEdit() {
        edited = true;
    }

    public boolean hasBeenDeleted() {
        return deleted;
    }

    public boolean hasBeenEdited() {
        return edited;
    }

    protected abstract void executeDelete() throws IOException;

    public abstract ForumContext getContext();


    public boolean equalCommands(ForumPost toCompare) {
        if (commands.size() != toCompare.commands.size())
            return false;
        for (int i = 0; i < commands.size(); ++i) {
            if (!commands.get(i).equals(toCompare.commands.get(i)))
                return false;
        }
        return true;
    }


    public List<Command> getCommands() {
        return commands;
    }

    public String getUrl() {
        return getContext().getPostUrl(thread.getThreadId(), getPage(), postId);
    }

    public int getPostId() {
        return postId;
    }

    public String getThreadId() {
        return thread.getThreadId();
    }

    public String getBoardId() {
        return thread.getBoardId();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ForumPost) {
            ForumPost post = (ForumPost)o;
            if (post.getPostId() == getPostId() && post.getContext().equals(getContext()))
                return true;
        }
        return false;
    }
}
