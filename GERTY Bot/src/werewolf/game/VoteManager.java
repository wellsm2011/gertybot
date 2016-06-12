package werewolf.game;

import java.io.Serializable;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import java.util.logging.Logger;

import werewolf.net.ForumUser;

/**
 * The VoteManager class tracks and displays votes for a given set of players and votes. It represents a record of all
 * the votes made in a single round of Werewolf.
 */
public class VoteManager implements Serializable {
    private final static Logger LOGGER = Logger.getLogger(VoteManager.class.getName());

    @SuppressWarnings("compatibility:4563764421772541428")
    private static final long serialVersionUID = -1511892114410847496L;


    private LinkedList<Vote> votes = new LinkedList<Vote>();
    private List<Player> players;
    private boolean multipleVotes = false;


    /**
     * Creates a new vote manager which may allow players to vote for multiple targets in the same round.
     *
     * @param players The list of current players.
     * @param multipleVotes True if players should be able to vote multiple times in a round.
     */
    public VoteManager(List<Player> players, boolean multipleVotes) {
        this.players = players;
        this.multipleVotes = multipleVotes;
    }

    /**
     * Creates a new vote manager without the ability for players to vote multiple targets.
     *
     * @param players The list of current players.
     */
    public VoteManager(List<Player> players) {
        this(players, false);
    }


    /**
     * Adds a vote to the tally.
     *
     * @param vote The cast vote to add.
     */
    public void placeVote(Vote vote) {
        boolean validVoter = false;
        boolean validTarget = false;

        // Check to see if we have a valid voter.
        if (vote.getVoter() != null) {
            if (players.contains(vote.getVoter())) {
                validVoter = true;
                vote.getVoter().madeVote();
            }
        }
        if (!validVoter) {
            LOGGER.warning("Unknown voter: " + vote + ". Ignoring vote.");
            return;
        }

        // Check to see if we have a valid target for the vote. (static user or player)
        if (vote.getTarget() != null && vote.getTarget().equals(StaticUser.NOLYNCH) ||
            vote.getTarget().equals(StaticUser.NOKING) || vote.getTarget().equals(StaticUser.NOVOTE) ||
            players.contains(vote.getTarget()))
            validTarget = true;
        if (!validTarget) {
            LOGGER.warning("Unknown vote target: " + vote + ". Ignoring vote.");
            return;
        }

        Iterator<Vote> iter = votes.descendingIterator();
        while (iter.hasNext()) {
            Vote voteCheck = iter.next();
            //Ensure that this vote is not the same as the player's previous vote. (ignore duplicates)
            if (voteCheck.getVoter().equals(vote.getVoter())) {
                if (voteCheck.getTarget().equals(vote.getTarget())) {
                    LOGGER.fine("Duplicate vote found, ignoring: " + vote);
                    return;
                }
                break; //Found player's last vote, and it wasn't a duplicate. Thus, allow this vote.
            }
        }
        votes.add(vote);
    }


    /**
     * @return The number of votes that the highest voted player has on them.
     */
    public int getHighestVotedCount() {
        Hashtable<User, LinkedList<Vote>> sortedVotes = getSortedVotes();

        for (int i = players.size(); i > 0; --i) {
            for (Player player : players) {
                if (sortedVotes.get(player).size() == i)
                    return i;
            }
        }
        return 0;
    }


    /**
     * @return A BB Code formatted tally of the current votes in the game.
     */
    public String getTally() {
        return getTally(true);
    }

    /**
     * @param includeLinks If true, the output is formatted with BB Code which allows viewers to click a vote to be
     * taken to the post where it occured.
     * @return A formatted tally of the current votes in the game.
     */
    public String getTally(boolean includeLinks) {
        Hashtable<User, LinkedList<Vote>> sortedVotes = getSortedVotes();
        StringBuilder output = new StringBuilder();

        for (int i = players.size(); i > 0; --i) {
            for (Player player : players) {
                //Get all the votes for a given player.
                LinkedList<Vote> votesFor = sortedVotes.get(player);
                if (votesFor.size() == i) {
                    output.append(player.getName() + " - " + i + " - ");
                    output.append(getFullVoteString(votesFor, ", ", true, includeLinks));
                    output.append("\n");
                }
            }
        }
        // Check for votes for non-players.
        for (StaticUser usr : StaticUser.LIST) {
            if (sortedVotes.get(usr).size() > 0) {
                output.append(usr.getName() + " - " + sortedVotes.get(usr).size() + " - ");
                output.append(getFullVoteString(sortedVotes.get(usr), ", ", true, includeLinks));
                output.append("\n");
            }
        }

        return output.toString();
    }

    /**
     * Transforms the list of votes into a hash table of linked lists which are indexed by voter.
     *
     * @return A hastable containing all the votes indexed by the player casting the vote.
     */
    private Hashtable<User, LinkedList<Vote>> getSortedVotes() {
        Hashtable<User, LinkedList<Vote>> sortedVotes = new Hashtable<User, LinkedList<Vote>>();
        LinkedList<ForumUser> applied = new LinkedList<ForumUser>();

        // Add all voting possibilities.
        sortedVotes.put(StaticUser.NOVOTE, new LinkedList<Vote>());
        sortedVotes.put(StaticUser.INCAPACITATED, new LinkedList<Vote>());
        sortedVotes.put(StaticUser.NOLYNCH, new LinkedList<Vote>());
        sortedVotes.put(StaticUser.NOKING, new LinkedList<Vote>());
        for (Player player : players)
            sortedVotes.put(player, new LinkedList<Vote>());


        Iterator<Vote> iter = votes.descendingIterator();
        while (iter.hasNext()) {
            Vote vote = iter.next();
            if (!applied.contains(vote.getVoter()) || multipleVotes) {
                if (sortedVotes.get(vote.getTarget()) == null) {
                    LOGGER.warning("Vote target removed from game: " + vote + ". Ignoring vote.");
                    continue;
                }
                sortedVotes.get(vote.getTarget()).addFirst(vote);
                applied.add(vote.getVoter());
            }
        }

        for (Player player : players) {
            if (!applied.contains(player) && player.isAlive()) {
                if (player.isInjured())
                    sortedVotes.get(StaticUser.INCAPACITATED).addFirst(new Vote(player, StaticUser.INCAPACITATED));
                else
                    sortedVotes.get(StaticUser.NOVOTE).addFirst(new Vote(player, StaticUser.NOVOTE));
            }
        }
        return sortedVotes;
    }


    /**
     * @return A BB Code formatted tally which puts the target instead of the voter first, followed by all the players
     * that voter has voted for.
     */
    public String getReverseTally() {
        return getReverseTally(true);
    }


    /**
     *
     * @param includeLinks True if the plaintext tally should be formatted with BB Code links to votes.
     * @return A formatted tally which puts the target instead of the voter first, followed by all the players
     * that voter has voted for.
     */
    public String getReverseTally(boolean includeLinks) {
        Hashtable<Player, LinkedList<Vote>> sortedVotes = new Hashtable<Player, LinkedList<Vote>>();
        StringBuilder output = new StringBuilder();

        for (Player player : players)
            sortedVotes.put(player, new LinkedList<Vote>());
        for (Vote vote : votes)
            sortedVotes.get(vote.getVoter()).add(vote);

        Enumeration<Player> plrs = sortedVotes.keys();
        LinkedList<Player> noVotes = new LinkedList<Player>();
        LinkedList<Player> incapacitated = new LinkedList<Player>();
        while (plrs.hasMoreElements()) {
            Player player = plrs.nextElement();
            LinkedList<Vote> votesMade = sortedVotes.get(player);
            if (player.isInjured()) {
                incapacitated.add(player);
                continue;
            }
            if (votesMade.size() == 0) {
                noVotes.add(player);
                continue;
            }
            output.append(player.getName() + ": ");
            output.append(getFullVoteString(votesMade, " -> ", false, includeLinks));
            output.append("\n");
        }

        return output.toString();
    }


    /**
     * @return The most voted user. In the event of a tie, LHLV is used as the tiebreaker.
     */
    public User getExpectedLhlvLynch() {
        Hashtable<User, LinkedList<Vote>> sortedVotes = getSortedVotes();
        int maxVotes = 0;
        LinkedList<User> highestVoted = new LinkedList<User>();

        for (Player player : players) {
            if (sortedVotes.get(player).size() == maxVotes) {
                highestVoted.add(player);
            } else if (sortedVotes.get(player).size() > maxVotes) {
                highestVoted.clear();
                highestVoted.add(player);
                maxVotes = sortedVotes.get(player).size();
            }
        }
        if (sortedVotes.get(StaticUser.NOLYNCH).size() >= maxVotes)
            return StaticUser.NOLYNCH;
        if (sortedVotes.get(StaticUser.NOKING).size() >= maxVotes) //Kingmaker equivilent of no lynch.
            return StaticUser.NOKING; //No king, no king, lalalalalala.
        if (highestVoted.size() == 1)
            return highestVoted.getFirst();
        Iterator<Vote> reverseVotes = votes.descendingIterator();
        while (reverseVotes.hasNext()) {
            if (highestVoted.remove(reverseVotes.next().getTarget()) && highestVoted.size() == 1)
                return highestVoted.getFirst();
        }
        return StaticUser.NOLYNCH;
    }


    /**
     * Returns the votes (ordered from begining to end) that the given player has made.
     *
     * @param plr
     * @return
     */
    public LinkedList<Vote> getVotes(String plr) {
        for (Player check : players) {
            if (check.getName().equals(plr))
                return getVotes(check);
        }
        throw new IllegalArgumentException("Unknown player: " + plr + ".");
    }


    /**
     * Returns the votes (ordered from begining to end) that the given player has made.
     *
     * @param plr
     * @return
     */
    public LinkedList<Vote> getVotes(Player plr) {
        LinkedList<Vote> output = new LinkedList<Vote>();

        //Ordered from begining to end.
        for (Vote vote : votes) {
            if (vote.getVoter().equals(plr))
                output.add(vote);
        }

        return output;
    }


    /**
     * @return The number of votes cast in this VoteManager.
     */
    public int length() {
        return votes.size();
    }


    /**
     *
     * @param votes List of votes to output.
     * @param delimiter Used between each vote.
     * @param postVoter True if the list should consist of voters istead of targets.
     * @param includeLink True if the BB Code link should be included for each vote.
     * @return The full vote string for a given series of votes.
     */
    private String getFullVoteString(List<Vote> votes, String delimiter, boolean postVoter, boolean includeLink) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < votes.size(); ++i) {
            if (i > 0)
                output.append(delimiter);
            output.append(getVoteString(votes.get(i), postVoter, includeLink));
        }
        return output.toString();
    }


    /**
     *
     * @param vote The vote to return.
     * @param postVoter True if the output should consist of the voter istead of the target.
     * @param includeLink True if the BB Code link should be included for this vote.
     * @return The vote string for a given vote.
     */
    private String getVoteString(Vote vote, boolean postVoter, boolean includeLink) {
        String name = vote.getTarget().getName();
        if (postVoter)
            name = vote.getVoter().getName();
        if (vote.post == null || !includeLink)
            return name;
        return "[url=" + vote.getVoteLink() + "]" + name + "[/url]";
    }

    /**
     * Resets the VoteManager, usually called when a new round starts.
     */
    public void reset() {
        votes.clear();
    }

    @Override
    public String toString() {
        return getTally(false);
    }
}
