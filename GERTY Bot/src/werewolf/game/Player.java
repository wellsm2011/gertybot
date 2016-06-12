package werewolf.game;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import werewolf.net.ForumPost;
import werewolf.net.ForumUser;


public class Player extends ForumUser {
    private final static Logger LOGGER = Logger.getLogger(Player.class.getName());

    @SuppressWarnings("compatibility:-2868866463434362966")
    private static final long serialVersionUID = 4363493461063986117L;

    private boolean                 alive = true;
    private int                     injured = 0;
    private LinkedList<PlayerEvent> data = new LinkedList<PlayerEvent>();
    private PlayerEvent             lastDeath = null;
    private LinkedList<Vote>        votesCast = new LinkedList<Vote>();
    private int                     inactiveVoteRounds = 0;
    private int                     inactivePostRounds = 0;
    private ForumPost               joinPost;

    /**
     * Creates a new player with the given forum user and join post.
     *
     * @param player The forum user to represent this player.
     * @param joinPost The post where the forum user either joined or was added to the game.
     */
    public Player(ForumUser player, ForumPost joinPost, int roundNumber) {
        super(player);
        LOGGER.fine("Player created: " + player);
        this.joinPost = joinPost;
        for (int i = 1; i < roundNumber; ++i)
            votesCast.add(new Vote(this, StaticUser.INCAPACITATED));
    }


    /**
     * Marks this player as replacing the given player, and copies the old player's data and status to this player.
     *
     * @param plr The player that this player is a replacement for.
     */
    public void replacePlr(Player plr) {
        LOGGER.fine("Player replacement: " + this + " for " + plr);
        data = plr.data;
        alive = plr.alive;
        votesCast = plr.votesCast;
        injured = plr.injured;
        lastDeath = plr.lastDeath;
    }

    /**
     * Performs post-round cleanup for this player. Called after the night phase each round.
     */
    public void endRound(Vote finalVote) {
        votesCast.add(finalVote);
        injured = Math.max(0, injured - 1);
        if (alive) {
            inactiveVoteRounds++;
            inactivePostRounds++;
        }
    }

    /**
     * @return A BBCode formatted list of all the players this player has voted for this game.
     */
    public String getVotes() {
        StringBuilder output = new StringBuilder(super.getName() + ": ");
        boolean       firstElement = true;

        for (Vote vote : votesCast) {
            if (!firstElement)
                output.append(" -> ");
            String voteStr = vote.getTarget().getName();
            if (vote.getVoteLink().length() > 0)
                voteStr = "[url=" + vote.getVoteLink() + "]" + voteStr + "[/url]";
            if (vote.getTarget() instanceof Player) {
                Player plr = (Player)vote.getTarget();
                if (!plr.isAlive()) {
                    voteStr = "[color=#FFBF80]" + plr.getContext().strike(voteStr) + "[/color]";
                    voteStr += " " + plr.getLastDeath();
                }
            }
            output.append(voteStr);
            firstElement = false;
        }

        return output.toString();
    }

    /**
     * Marks this player as being injured for one round.
     */
    public void injure() {
        injure(1);
    }

    /**
     * Marks this player as being injured for the specified number of rounds.
     */
    public void injure(int rounds) {
        injured = Math.max(1, injured) + rounds;
    }

    /**
     * @return True if the player is currently injured, false otherwise.
     */
    public boolean isInjured() {
        return injured > 0;
    }

    /**
     * Signals that this player has made a post. Used when calculating inactivity.
     */
    public void madePost() {
        inactivePostRounds = 0;
    }

    /**
     * Signals that this player has made a vote. Used when calculating inactivity.
     */
    public void madeVote() {
        inactiveVoteRounds = 0;
    }

    /**
     * @return The number of rounds since this player last posted. Resets count when the player dies or revives.
     */
    public int inactivePostRounds() {
        return inactivePostRounds;
    }

    /**
     * @return The number of rounds since this player last voted.
     * Returns 0 if the player has voted in the current round.
     */
    public int inactiveVoteRounds() {
        return inactiveVoteRounds;
    }


    /**
     * Marks this player as dead.
     *
     * @param evt The event surrounding this player's death.
     */
    public void kill(PlayerEvent evt) {
        addData(evt);
        alive = false;
    }


    /**
     * Marks this player as alive again.
     *
     * @param evt The event surrounding this player's death.
     * @return False if the player was already alive, true otherwise.
     */
    public boolean revive(PlayerEvent evt) {
        if (alive)
            return false;
        addData(evt);
        inactivePostRounds = 0;
        inactiveVoteRounds = 0;
        alive = true;
        return true;
    }


    /**
     * @return True if the player is currently alive.
     */
    public boolean isAlive() {
        return alive;
    }


    /**
     * @return A plaintext log of the events that have happened to this player.
     */
    public String getData() {
        if (data.isEmpty())
            return "";

        StringBuilder output = new StringBuilder();
        output.append(" - ");
        boolean isFirst = true;
        for (PlayerEvent event : data) {
            if (!isFirst)
                output.append(", ");
            output.append(event.getData());
            isFirst = false;
        }
        return output.toString();
    }

    /**
     * @return A shortened link to the last death of the player, or null if the player has not died yet.
     */
    public String getLastDeath() {
        if (lastDeath == null)
            return "";
        return lastDeath.makeLink(lastDeath.getRound());
    }


    /**
     * @return A BBCode formatted log of the events that have happened to this player.
     */
    public String getForumData() {
        if (data.isEmpty())
            return "";

        StringBuilder output = new StringBuilder();
        output.append(" - ");
        boolean isFirst = true;
        for (PlayerEvent event : data) {
            if (!isFirst)
                output.append(", ");
            output.append(event.makeLink(event.getData()));
            isFirst = false;
        }
        return output.toString();
    }


    /**
     * @param evt The event this player was involved in.
     */
    public void addData(PlayerEvent evt) {
        data.add(evt);
    }


    /**
     * @return The post where this player joined.
     */
    public ForumPost getJoinPost() {
        return joinPost;
    }
}
