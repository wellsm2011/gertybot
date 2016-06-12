package werewolf.game;

import java.io.Serializable;

import werewolf.net.ForumPost;
import werewolf.net.ForumUser;

public class Vote implements Serializable {
    //private final static Logger LOGGER = Logger.getLogger(Vote.class.getName());

    @SuppressWarnings("compatibility:4944478343116558769")
    private static final long serialVersionUID = -479947774406039167L;


    private Player voter;
    private User target;
    public final ForumPost post;


    /**
     * Creates a new vote that is not ascociated with any forum post.
     *
     * @param voter The voting user.
     * @param target The target user.
     */
    public Vote(Player voter, User target) {
        this(voter, target, null);
    }


    /**
     * Creates a new vote that is ascociated with a forum post.
     *
     * @param voter The voting user.
     * @param target The target user.
     * @param post The post where the vote was cast.
     */
    public Vote(Player voter, User target, ForumPost post) {
        this.voter = voter;
        this.target = target;
        this.post = post;
    }


    /**
     * @return The voting user.
     */
    public Player getVoter() {
        return voter;
    }

    /**
     * @return The target user.
     */
    public User getTarget() {
        return target;
    }

    /**
     * @return A url link to the post where the vote was cast, or an empty string if no post is ascociated with this
     * vote.
     */
    public String getVoteLink() {
        if (post != null && post.getPostId() > 0)
            return post.getUrl();
        return "";
    }


    @Override
    public String toString() {
        return voter + " -> " + target;
    }
}
