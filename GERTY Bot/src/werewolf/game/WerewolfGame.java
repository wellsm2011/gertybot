package werewolf.game;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import werewolf.net.Command;
import werewolf.net.ForumContext;
import werewolf.net.ForumPost;
import werewolf.net.ForumThread;
import werewolf.net.ForumUser;
import werewolf.net.ThreadManager;
import werewolf.net.neon.NeonContext;


public class WerewolfGame implements ThreadManager {
    private static final Logger LOGGER = Logger.getLogger(WerewolfGame.class.getName());

    private ForumThread thread;
    private ArrayList<Player> players = new ArrayList<Player>();
    private VoteManager votes = new VoteManager(players);
    private ForumUser host = null;
    private ForumUser cohost = null;
    private StringBuilder pastVotes = new StringBuilder();
    private String[] gameId;

    private int round = 0;
    private boolean isDay = false;
    private boolean openSignups = false;
    private boolean hasStarted = false;
    private boolean multipleVotes = false;
    private boolean containsHostCommands = false;
    private boolean resetVotesOnRoundChange = true;

    private String storyPosts = "";


    private byte lynchType = 0; //0: Undefined, 1: Timed, 2: Single, 3: Majority, 4: Decremental.
    private int roundLength = 0;
    private int percentMajority = 0;
    private DateTime startTime = DateTime.now();

    private boolean stateChange = false;
    private ForumPost lastPost = null;
    private boolean votesReset = false;


    /**
     * Creates a new Werewolf game. Requires a ForumThread to use as a base.
     *
     * @param init The forum thread that this game is played in.
     * @throws IOException if any of the underlying network operations throw an exception.
     */
    public WerewolfGame(ForumThread init) throws IOException {
        thread = init;
        host = thread.getPosts().get(0).getPoster();
        gameId = GameID.get(init.getThreadId());

        // Evaluate all posts for game commands.
        ForumPost post = thread.nextPost();
        while (post != null) {
            parseCommands(post);
            post = thread.nextPost();
        }
        LOGGER.info("Game initalized: " + thread);
    }


    /**
     * Checks to see if the bot needs to post in the main thread. Also updates the bot's information on the game.
     *
     * @return True if the bot made a new post, false otherwise.
     * @throws IOException If any of the underlying network operations throw an error.
     */
    public boolean update() throws IOException {
        thread.refresh();
        ForumPost post = thread.nextPost();
        while (post != null) { // Parse any new commands.
            parseCommands(post);
            post = thread.nextPost();
        }
        LOGGER.info("Game parsed: " + thread.getTitle());

        ForumContext context = thread.getContext();

        //Check to see if we need to post again...
        if (!containsHostCommands || (hasLastPost() && !stateChange && lastPost.getPage() == thread.pages()))
            return false;

        //Construct post.
        String postString = "";
        if (isDay && hasStarted)
            postString = getVoteString() + "\n" + getInactives();
        if (pastVotes.length() > 0)
            postString += "\n\n" + context.spoiler("Voting History", pastVotes.toString());
        if (round > 2)
            postString += "\n\n" + context.spoiler("Final Vote History", getFinalVoteHistory());
        if (postString.length() > 0 && isDay && hasStarted)
            postString += "\n\n" + context.spoiler("Players", getPlayerStatus());
        else
            postString =
                context.header("Players") + "\n" + getPlayerStatus() + "\n\n" + getInactives() + "\n\n" + postString;
        if (storyPosts.length() > 0 && hasStarted)
            postString += "\n\n" + context.header("Story Posts") + "[list]" + storyPosts + "[/list]\n";

        postString += "\n\n" + getPhaseString();
        if (context.RULES_URL != null) {
            postString += "\n[url=" + context.RULES_URL + "]Rules Thread[/url]\n";
        }
        if (context.allowPMs()) {
            postString +=
                "\nGame ID: " + gameId[1] + " (" + gameId[0] + ")\n" +
                "[i]Use the game id or it's abbreviation as the subject of any PMs to the bot regarding this game.[/i]";
        }
        try {
            if (hasLastPost())
                lastPost.delete();
        } catch (IOException ex) {
            LOGGER.warning("Cannot delete post in " + thread.getTitle() + ": " + ex.getMessage());
            ex.printStackTrace();
        }

        // Make post.
        thread.post(postString);
        stateChange = false;
        return true;
    }


    private boolean hasLastPost() {
        return lastPost != null && !lastPost.hasBeenDeleted();
    }


    private String getFinalVoteHistory() {
        StringBuilder output = new StringBuilder();

        for (Player plr : players) {
            output.append(plr.getVotes());
            output.append("\n");
        }

        return output.toString();
    }


    /**
     * @return A string list of all the inactive players in the game.
     */
    private String getInactives() {
        StringBuilder output = new StringBuilder();
        LinkedList<Player> inactivePosters = getInactivePosters();
        LinkedList<Player> inactiveVoters = getInactiveVoters();

        //TODO: Figure out how to format the inavtives list in a more readable way.
        if (!inactivePosters.isEmpty()) {
            output.append("Inactive Posters: ");
            boolean isFirst = true;
            for (Player plr : inactivePosters) {
                if (!isFirst)
                    output.append(", ");
                output.append(plr.getName() + " (" + plr.inactivePostRounds() + " rounds)");
                isFirst = false;
            }
            if (!inactiveVoters.isEmpty())
                output.append("\n");
        }


        if (!inactiveVoters.isEmpty()) {
            output.append("Inactive Voters: ");
            boolean isFirst = true;
            for (Player plr : inactiveVoters) {
                if (!isFirst)
                    output.append(", ");
                output.append(plr.getName() + " (" + plr.inactiveVoteRounds() + " rounds)");
                isFirst = false;
            }
        }

        return output.toString();
    }

    private LinkedList<Player> getInactivePosters() {
        LinkedList<Player> output = new LinkedList<Player>();
        for (Player plr : players) {
            if (plr.inactivePostRounds() > 1 && plr.isAlive())
                output.add(plr);
        }
        return output;
    }

    private LinkedList<Player> getInactiveVoters() {
        LinkedList<Player> output = new LinkedList<Player>();
        for (Player plr : players) {
            if (plr.inactiveVoteRounds() > 1 && plr.isAlive())
                output.add(plr);
        }
        return output;
    }

    /**
     * @return A list of all current players, along with all data about each player so far this game.
     */
    private String getPlayerStatus() {
        StringBuilder output = new StringBuilder();
        if (players.isEmpty())
            return "None.";
        for (int i = 0; i < players.size(); ++i) {
            Player plr = players.get(i);
            String name = plr.getName();
            if (!plr.isAlive())
                name = "[color=#FFBF80]" + thread.getContext().strike(name) + "[/color]";
            //eg, 02: Bob - Lynched R4, Shot R5
            ForumPost joinPost = plr.getJoinPost();
            String url = plr.getContext().getPostUrl(joinPost.getThreadId(), joinPost.getPage(), joinPost.getPostId());
            output.append(String.format("[url=%s]%02d[/url]: %s %s%n", url, i + 1, name, plr.getForumData()));
        }
        return output.toString();
    }

    /**
     * @return The current phase of the game, such as "R1 Day" or "R5 Night" or "Pregame Setup"
     */
    private String getPhaseString() {
        if (!hasStarted)
            return "Phase: Pregame Setup";
        if (isDay)
            return "Phase: R" + round + " Day";
        return "Phase: R" + round + " Night";
    }


    /**
     * @return A list of the current tally and reverse tally for this round. Includes a list of players not voting.
     */
    private String getVoteString() {
        ForumContext context = thread.getContext();
        String voteString = context.header("Lynch Tally") + "\n" + votes.getTally();
        if (context.allowExpectedLynch() && lynchType < 3)
            voteString += "\nExpected LHLV Lynch: [b]" + votes.getExpectedLhlvLynch().getName() + "[/b]";
        //0: Undefined, 1: Timed, 2: Single, 3: Majority, 4: Decremental.
        switch (lynchType) {
        case 1:
        case 2:
            if (roundLength > 0) {
                voteString += "\nLynch will end on " + getEndTime().toString("E 'at' hh:mm a z.");
                if (!votingOpen(DateTime.now()))
                    voteString += " [color=#FF0000](Time expired)[/color]";
            }
            break;
        case 3:
        case 4:
            voteString += "\n" + getVotesForLynch(DateTime.now()) + " votes required for lynch.";
            if (lynchType == 4) {
                DateTime nextReduction = startTime.plusHours(roundLength);
                while (nextReduction.isBefore(DateTime.now()))
                    nextReduction = nextReduction.plusHours(roundLength);
                voteString += " This number will go down by one on " + nextReduction.toString("E 'at' hh:mm a z.");
            }
            break;
        }
        voteString += "\nTotal: " + livingPlayerCount() + " living players.\n";
        if (votes.length() > 0 && lynchType != 2)
            voteString += "\n\n" + context.header("Reverse Tally") + "\n" + votes.getReverseTally();
        return voteString + "\n";
    }


    /**
     * @return The number of players in the game who are considered alive.
     */
    private int livingPlayerCount() {
        int count = 0;
        for (Player plr : players) {
            if (plr.isAlive())
                count++;
        }
        return count;
    }


    /**
     * @return The user marked as host for this round. (eg, the first poster in the thread)
     */
    public ForumUser getHost() {
        return host;
    }

    /**
     * @return The user marked as cohost by the host, or null if no cohost exists.
     */
    public ForumUser getCohost() {
        return cohost;
    }

    /**
     * @return The thread in which this game takes place.
     */
    public ForumThread getThread() {
        return thread;
    }


    private void parseCommands(ForumPost post) throws IOException {
        boolean containsCommand = false;
        for (Player plr : players) {
            if (plr.equals(post.getPoster()))
                plr.madePost();
        }
        for (Command command : post.getCommands()) {
            command.startCheck();
            boolean isCommand = true;
            String cmd = command.getCommand().toLowerCase();
            // Search through all possible commands and execute any valid ones.
            if (cmd.matches("^(join|in)$")) // Adds the user to the game. Requires signups open and pregame setup.
                joinGame(command);
            else if (cmd.matches("^(leave|quit|out)$")) // Removes the user fromt the game.
                leaveGame(command);
            else if (cmd.matches("^(add)$")) // Forces a user to join the game. Only usable by the host. Requires the
                addToGame(command); // forced user to have posted at least once in the game thread.
            else if (cmd.matches("^(replace)$")) // Replaces a user with another user. Only usable by the host.
                replaceInGame(command);
            else if (cmd.matches("^(alias)$"))
                addAlias(command);
            else if (cmd.matches("^(remove)$")) // Removes a player from the game completely. Only usable by the host.
                removeFromGame(command);
            else if (cmd.matches("^(vote|lynch|banish)$")) // Logs the user as voting for another player. Requires day
                makeVote(command); // and can only be used by a player.
            else if (cmd.matches("^(unvote|abstain)$")) // Logs the user as not voting. Requires day and can only be
                removeVote(command); // used by a player.
            else if (cmd.matches("^(sign\\-?ups?)$")) // Marks signups as open or closed. May only be used by the host.
                changeSignups(command);
            else if (cmd.matches("^(dusk|night)$")) // Marks that the game is now in night phase, jumping from the
                duskPost(command); // current phase forward to night. Only usable by the host.
            else if (cmd.matches("^(dawn|day)$")) // Marks that the game is now in day phase, jumping from the
                dawnPost(command); // current phase forward to dau. Only usable by the host.
            else if (cmd.matches("^(start)$")) // Starts the game and puts it into round zero. Automatically closes
                startGame(command); // signups. Only usable by the host.
            else if (cmd.matches("^(kill)$")) // Marks a player as killed and removes them from
                killPlayer(command); // the game. Unlike remove, keeps their data. Only usable by host.
            else if (cmd.matches("^(modkill)$")) // Marks a player as modkilled and removes them from
                modkillPlayer(command); // the game. Unlike remove, keeps their data. Only usable by host.
            else if (cmd.matches("^(revive|raise)$")) // Marks a previously killed player as alive again. Only usable by
                revivePlayer(command); // the host.
            else if (cmd.matches("^(log|data|note)$")) // Logs some extra data about a player, but does not change their
                logPlayerData(command); // status in any way. Only usable by the host.
            else if (cmd.matches("^(injure|hospitalize)$")) // Logs a player as having been injured and unable to vote.
                injurePlayer(command); // Bars the player from voting in the next round. Host only.
            else if (cmd.matches("^(co\\-?host)$")) // Sets a player as the cohost of the game, giving them all the
                setCohost(command); // powers the host has. Only usable by the host.
            else if (cmd.matches("^(vote|lynch)(ing)?type$")) // Sets the current lynch type. Supported types:
                changeLynchType(command); // Majority [<%>], Timed <hours>, Single [<hours>], Decremantal <hours> [<%>].
            else if (cmd.matches("^(end)$")) // Marks a game as being complete. May be used with a list of winning
                endGame(command); // players to have the bot update win/loss records. Only usable by the host.
            else if (cmd.matches("^(story(post)?|flag|title)$")) // Marks a post as being importiant.
                setStoryPost(command); // Optionally adds a title to the post. Only usable by the host.
            else if (cmd.matches("^(re(hash|start|set))$")) { //Re-parse the thread to update any edited posts.
                try {
                    if (hasLastPost())
                        lastPost.delete();
                    if (post.getCommands().size() > 1)
                        command.invalidate("rehash complete");
                    else
                        post.delete();
                    reset();
                    break;
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, ex.getMessage(), ex);
                }
            } else { //Unknown command found.
                isCommand = false;
                command.invalidate("unknown command");
            }

            if (isCommand && command.isMarkedHidden() &&
                isHost(command.getUser())) // 'Hidden' commands are commands with two
                command.hide(); // brackets instead of one. eg, [[command]]
            if (isCommand &&
                command.isChecking()) //Means this command was previously invalid, but has since become valid.
                command.validate();

            containsCommand = containsCommand || isCommand;
        }
        if (post.getPoster().equals(host) && containsCommand)
            containsHostCommands = true;
        if (post.getPoster().equals(thread.getContext().LOGIN_USER) && !containsCommand) {
            lastPost = post;
            stateChange = false;
        }
    }

    /**
     * Resets the entire game and parses the thread from scratch. Usually does not require the thread to be reloaded.
     *
     * @throws IOException If any of the underlying network calls throw an error.
     */
    public void reset() throws IOException {
        thread.reset();
        players.clear();
        votes = new VoteManager(players);
        pastVotes = new StringBuilder();

        round = 0;
        isDay = false;
        openSignups = false;
        hasStarted = false;
        multipleVotes = false;
        resetVotesOnRoundChange = true;

        stateChange = false;
        lastPost = null;

        //Search through thread again to recreate game.
        ForumPost post = thread.nextPost();
        while (post != null) {
            parseCommands(post);
            post = thread.nextPost();
        }
    }

    /**
     * @param user The user to check.
     * @return True if the given player is the host, cohost or the bot itself; false otherwise.
     */
    public boolean isHost(ForumUser user) {
        return user != null && (user.equals(host) || user.equals(cohost) || isMe(user));
    }

    /**
     * @param name The name, either partial or whole, of the player to search for.
     * @return The player object represented by the input string, or null if no player was found or if multiple players
     * matched the given name.
     */
    public Player getPlayer(String name) {
        return (Player) getObject(name, players);
    }


    private static boolean isMe(ForumUser user) {
        return user.equals(user.getContext().LOGIN_USER);
    }


    private ForumUser getUser(String name) {
        return getObject(name, thread.getContext().USERS.getKnownUsers());
    }


    private Player getPlayerById(int id) {
        return (Player) getObjectById(id, players);
    }


    private ForumUser getObject(String name, Collection<? extends ForumUser> list) {
        for (ForumUser user : list) {
            if (user.getName().toLowerCase().equals(name.toLowerCase()))
                return user;
        }


        ForumUser found = null;
        for (ForumUser user : list) {
            if (user.getName().toLowerCase().startsWith(name.toLowerCase())) {
                if (found != null)
                    return null;
                found = user;
            }
        }
        if (found != null)
            return found;


        for (ForumUser user : list) {
            for (String alias : user.getAliases()) {
                if (alias.toLowerCase().contains(name.toLowerCase())) {
                    if (found != null && found != user)
                        return null;
                    found = user;
                }
            }
        }
        if (found != null)
            return found;


        for (ForumUser user : list) {
            if (user.getName().toLowerCase().contains(name.toLowerCase())) {
                if (found != null)
                    return null;
                found = user;
            }
        }

        return found;
    }


    private ForumUser getObjectById(int id, List<? extends ForumUser> list) {
        for (ForumUser user : list) {
            if (user.getUserId() == id)
                return user;
        }
        return null;
    }


    private void addAlias(Command cmd) {
        String[] params = cmd.getParams().split(", ?");
        if (params.length < 2) {
            cmd.invalidate("not enough params");
            return;
        }

        ForumUser aliasPlayer;
        try {
            aliasPlayer = ForumUser.getUserFor(Integer.parseInt(params[0]), thread.getContext());
            if (aliasPlayer == null) {
                cmd.invalidate("unknown user, " + params[0]);
                return;
            }
        } catch (NumberFormatException ex) {
            cmd.invalidate("syntax: alias <id>, <alias>");
            return;
        }

        aliasPlayer.addAlias(params[1]);
    }


    private void joinGame(Command cmd) {
        if (!openSignups) {
            cmd.invalidate("signups closed");
            return;
        }

        for (Player plr : players) {
            if (plr.equals(cmd.getUser())) {
                cmd.invalidate("duplicate player");
                return;
            }
        }

        players.add(new Player(cmd.getUser(), cmd.getPost(), round));
        stateChange = true;
    }

    private void endGame(Command cmd) {
        try {
            if (!isHost(cmd.getUser())) {
                cmd.invalidate("invalid access");
                return;
            }

            String[] params = cmd.getParams().split(", ?");
            LinkedList<ForumUser> winners = new LinkedList<ForumUser>();
            LinkedList<ForumUser> losers = new LinkedList<ForumUser>();
            LinkedList<ForumUser> hosts = new LinkedList<ForumUser>();

            for (String plr : params)
                winners.add(getPlayer(plr));

            if (winners.size() > 0) {
                losers.addAll(players);
                losers.removeAll(winners);
                hosts.add(host);
                if (cohost != null)
                    hosts.add(cohost);

                try {
                    thread.getContext().RECORD.addGame(thread, hosts, winners, losers);
                } catch (NullPointerException e) {
                    LOGGER.log(Level.INFO, "Invalid context to record results: " + thread.getContext().toString() + ".",
                               e);
                }

                try {
                    //thread.getContext().SIGNUPS.endGame(thread.getThreadId());
                } catch (NullPointerException e) {
                    LOGGER.log(Level.INFO,
                               "Invalid context to complete signups: " + thread.getContext().toString() + ".", e);
                }
            }
        } catch (IOException | NullPointerException e) {
            LOGGER.log(Level.WARNING, "Unable to complete end command in " + thread.toString() + ".", e);
        }
    }

    private void addToGame(Command cmd) {
        String[] params = cmd.getParams().split(", ?");
        ForumUser newPlayer = getUser(params[0]);
        try {
            if (newPlayer == null && params.length > 1) {
                int userID = Integer.parseInt(params[1].replaceAll("[^0-9]", ""));
                newPlayer = ForumUser.getUserFor(userID, thread.getContext());
                if (newPlayer == null)
                    newPlayer = ForumUser.getUserFor(userID, params[0], NeonContext.INSTANCE);
            }
        } catch (NumberFormatException ex) {
            LOGGER.info("Error parsing add command: " + ex.getMessage());
            cmd.invalidate("syntax error: [add name, id]");
        }
        if (!isHost(cmd.getUser())) {
            cmd.invalidate("invalid access");
            return;
        }
        if (newPlayer == null)
            return;
        for (Player plr : players) {
            if (plr.equals(newPlayer)) {
                cmd.invalidate("duplicate player");
                return;
            }
        }
        players.add(new Player(newPlayer, cmd.getPost(), round));
        stateChange = true;
    }


    private void replaceInGame(Command cmd) {
        String[] params = cmd.getParams().split(", ?");
        ForumUser newPlayer = getUser(params[1]);
        Player oldPlayer = getPlayer(params[0]);
        if (newPlayer == null || oldPlayer == null)
            return;
        for (Player plr : players) {
            if (plr.equals(newPlayer)) {
                cmd.invalidate("duplicate player");
                return;
            }
        }

        Player plr = new Player(newPlayer, cmd.getPost(), round);
        plr.replacePlr(oldPlayer);
        players.set(players.indexOf(oldPlayer), plr);
        stateChange = true;
    }


    private void makeVote(Command cmd) {
        if (!isDay) {
            cmd.invalidate("not day");
            return;
        }
        try {
            Player voter = getPlayerById(cmd.getUser().getUserId());
            User voted = getPlayer(cmd.getParams().replaceAll("^ *\\- *", ""));

            if (cmd.getParams().toLowerCase().matches("no[ \\-]?lynch"))
                voted = StaticUser.NOLYNCH;
            if (cmd.getParams().toLowerCase().matches("no[ \\-]?king"))
                voted = StaticUser.NOKING;
            if (voted == null)
                cmd.invalidate("unknown target");
            else if (voter == null)
                cmd.invalidate("unknown voter");
            else if (voter.isInjured()) // Injured players are not allowed to vote.
                cmd.invalidate("injured voter");
            else if (!voter.isAlive())
                cmd.invalidate("voter dead");
            else if (voted instanceof Player && !((Player) voted).isAlive())
                cmd.invalidate("target dead");
            else if (cmd.getPost().getPostTime() != null && !votingOpen(cmd.getPost().getPostTime()))
                cmd.invalidate("voting has ended");
            else if (lynchType == 2 && votes.getVotes(voter).size() > 0)
                cmd.invalidate("can't change vote");
            else {
                votes.placeVote(new Vote(voter, voted, cmd.getPost()));
                stateChange = true;
            }
        } catch (IndexOutOfBoundsException ex) {
            cmd.invalidate("unknown player");
        }
    }


    private void removeVote(Command cmd) {
        Player player = getPlayer(cmd.getUser().getName());


        if (player == null)
            cmd.invalidate("unknown player");
        else if (!isDay)
            cmd.invalidate("not day");
        else if (cmd.getPost().getPostTime() != null && !votingOpen(cmd.getPost().getPostTime()))
            cmd.invalidate("voting has ended");
        else if (lynchType == 2 && votes.getVotes(player).size() > 0)
            cmd.invalidate("can't change vote");
        else {
            if (player.isInjured()) // Injured players are not allowed to vote.
                return;

            votes.placeVote(new Vote(player, StaticUser.NOVOTE, cmd.getPost()));
            stateChange = true;
        }
    }


    private void logPlayerData(Command cmd) {
        if (!isHost(cmd.getUser())) {
            cmd.invalidate("invalid access");
            return;
        }
        // Command comes in the form "log <player>, <reason>"
        String[] params = cmd.getParams().split(", ?", 2);
        Player target = getPlayer(params[0]);

        if (params.length < 2)
            cmd.invalidate("missing params");
        else if (target == null)
            cmd.invalidate("unknown player");
        else
            target.addData(new PlayerEvent(params[1].trim(), round, cmd.getPost().getUrl()));
    }


    private void changeSignups(Command cmd) {
        if (!isHost(cmd.getUser()))
            cmd.invalidate("invalid access");
        else if (cmd.getParams().toLowerCase().contains("open"))
            openSignups = true;
        else if (cmd.getParams().toLowerCase().contains("closed"))
            openSignups = false;
        else
            cmd.invalidate("incomplete command");
    }


    private void duskPost(Command cmd) {
        if (!isHost(cmd.getUser())) {
            cmd.invalidate("invalid access");
            return;
        }
        if (!hasStarted)
            dawnPost(cmd);
        openSignups = false;
        hasStarted = true;
        isDay = false;
        checkVoteReset();
    }


    private void dawnPost(Command cmd) {
        if (!isHost(cmd.getUser())) {
            cmd.invalidate("invalid access");
            return;
        }
        if (!hasStarted)
            startGame(cmd);
        checkVoteReset();
        votesReset = false;
        isDay = true;
        round += 1;
    }


    private void checkVoteReset() {
        if (votesReset)
            return;

        for (Player plr : players) { //Record each player's final vote.
            LinkedList<Vote> plrVotes = votes.getVotes(plr);
            if (plr.isInjured() || !plr.isAlive())
                plr.endRound(new Vote(plr, StaticUser.INCAPACITATED));
            else if (!plrVotes.isEmpty())
                plr.endRound(plrVotes.getLast());
            else
                plr.endRound(new Vote(plr, StaticUser.NOVOTE));
        }
        if (round > 0 && resetVotesOnRoundChange) {
            if (pastVotes.length() > 0)
                pastVotes.insert(0, "\n--------------------\n\n");
            pastVotes.insert(0, thread.getContext().header("R" + round) + " " + getVoteString());
            votes.reset();
        }
        votesReset = true;
    }


    private void startGame(Command cmd) {
        if (!isHost(cmd.getUser())) {
            cmd.invalidate("invalid access");
            return;
        }
        hasStarted = true;
        openSignups = false;
    }


    private void killPlayer(Command cmd) {
        if (!isHost(cmd.getUser())) {
            cmd.invalidate("invalid access");
            return;
        }
        String[] params = cmd.getParams().split(", ?", 2); //Syntax: <target>[, <message>]
        Player target = getPlayer(params[0]);
        if (target != null) {
            String message = "Killed";
            if (params.length > 1)
                message = params[1];

            target.kill(new PlayerEvent(message, round, cmd.getPost().getUrl()));
            stateChange = true;
        }
    }


    private void modkillPlayer(Command cmd) {
        if (!isHost(cmd.getUser())) {
            cmd.invalidate("invalid access");
            return;
        }
        String[] params = cmd.getParams().split(", ?", 2); //Syntax: <target>[, <message>]
        Player target = getPlayer(params[0]);
        if (target != null) {
            String message = "Modkilled";
            if (params.length > 1) {
                message = params[1];
                if (!params[1].trim().toLowerCase().startsWith("modkilled for"))
                    message = "Modkilled for " + message;
            }

            target.kill(new PlayerEvent(message, round, cmd.getPost().getUrl()));
            stateChange = true;
        }
    }


    private void revivePlayer(Command cmd) {
        Player target = getPlayer(cmd.getParams());

        if (!isHost(cmd.getUser()))
            cmd.invalidate("invalid access");
        else if (target == null)
            cmd.invalidate("unknown player");
        else if (target.isAlive())
            cmd.invalidate("target not dead");
        else
            stateChange |= target.revive(new PlayerEvent("Revived", round, cmd.getPost().getUrl()));
    }


    private void setStoryPost(Command cmd) {
        if (!isHost(cmd.getUser()))
            cmd.invalidate("invalid access");
        else if (cmd.getParams().length() < 1)
            storyPosts += "[url=" + cmd.getPost().getUrl() + "] Round " + round + "[/url]\n";
        else
            storyPosts += "[*][url=" + cmd.getPost().getUrl() + "]" + cmd.getParams() + "[/url]\n";
    }


    private void setCohost(Command cmd) {
        if (!isHost(cmd.getUser())) {
            cmd.invalidate("invalid access");
            return;
        }
        String[] params = cmd.getParams().split(", ?", 2); // Syntax: <name>[, <user ID>]
        ForumUser newCohost = getUser(params[0]);
        try {
            if (newCohost == null && params.length > 1) {
                int userID = Integer.parseInt(params[1].replaceAll("[^0-9]", ""));
                newCohost = ForumUser.getUserFor(userID, thread.getContext());
                if (newCohost == null)
                    newCohost = ForumUser.getUserFor(userID, params[0], NeonContext.INSTANCE);
            }
        } catch (NumberFormatException ex) {
            LOGGER.info("Error parsing cohost command: " + ex.getMessage());
        }
        if (newCohost != null)
            cohost = newCohost;
        else
            cmd.invalidate("unknown user");
    }


    private void removeFromGame(Command cmd) {
        if (!isHost(cmd.getUser())) {
            cmd.invalidate("invalid access");
            return;
        }
        stateChange = players.remove(getPlayer(cmd.getParams()));
    }


    /**
     * Changes the lynch manager for the game. By default, the bot does not participate in lynch tracking beyond votes.
     * <P>Accepted types:<BR>
     * <B>timed [hours]</B> - Causes the bot to stop accepting votes a given number of hours after each [dawn] post.
     * Default: 48 hours.<BR>
     * <B>single [hours]</B> - Causes the bot to not accept vote changes, in addition to the timed lynch above.
     * Default: No Timelimit<BR>
     * <B>majority [percent]</B> - Causes the bot to stop accepting votes once a majority of players have voted for the same
     * target. Default: 100% (track only; don't disable votes).<BR>
     * <B>decremental [hours [percent]]</B> - Causes the bot to track the current number of votes required for lynch, in
     * addition to majority lynch above. Default: 24 hours, 100% (track only; don't disable votes).
     *
     * @param cmd The command object that is attempting to call the command.
     */
    private void changeLynchType(Command cmd) {
        if (!isHost(cmd.getUser())) {
            cmd.invalidate("invalid access");
            return;
        }
        if (cmd.getPost().getPostTime() == null) {
            cmd.invalidate("error: cannot parse timestamps");
            return;
        }
        String[] params = cmd.getParams().split(" ", 2); // Syntax: <type>[ <options>]
        if (params[0].matches("timed")) {
            if (params.length == 1)
                roundLength = 48;
            else {
                try {
                    roundLength = Integer.parseInt(params[1]);
                } catch (NumberFormatException ex) {
                    cmd.invalidate("cannot parse hours: " + params[1]);
                    return;
                }
            }
            lynchType = 1; //Set lynch type to timed.
        } else if (params[0].matches("single")) {
            if (params.length == 1)
                roundLength = 48;
            else {
                try {
                    roundLength = Integer.parseInt(params[1]);
                } catch (NumberFormatException ex) {
                    cmd.invalidate("cannot parse hours: " + params[1]);
                    return;
                }
            }
            lynchType = 2; //Set lynch type to single vote.
        } else if (params[0].matches("majority")) {
            if (params.length == 1)
                percentMajority = 100;
            else {
                try {
                    percentMajority = Integer.parseInt(params[1].replace("%", ""));
                } catch (NumberFormatException ex) {
                    cmd.invalidate("cannot parse percent: " + params[1]);
                    return;
                }
            }
            lynchType = 3; //Set lynch type to majority.
        } else if (params[0].matches("decremental")) {
            if (params.length < 3)
                percentMajority = 100;
            else {
                try {
                    percentMajority = Integer.parseInt(params[2].replace("%", ""));
                } catch (NumberFormatException ex) {
                    cmd.invalidate("cannot parse percent: " + params[2]);
                    return;
                }
            }
            if (params.length < 2)
                roundLength = 48;
            else {
                try {
                    roundLength = Integer.parseInt(params[1]);
                } catch (NumberFormatException ex) {
                    cmd.invalidate("cannot parse hours: " + params[1]);
                    return;
                }
            }
            lynchType = 4; //Set lynch type to decremental.
        } else
            cmd.invalidate("unknown type: " + params[0]);
    }


    private void leaveGame(Command cmd) {
        if (players.remove(cmd.getUser()))
            stateChange = true;
        else
            cmd.invalidate("unknown player");
    }


    private void injurePlayer(Command cmd) {
        String[] params = cmd.getParams().split(", ?", 2);
        Player target = getPlayer(params[0]);
        if (!isHost(cmd.getUser()))
            cmd.invalidate("invalid access");
        else if (target == null)
            cmd.invalidate("unknown player");
        else {
            int rounds = 1;
            if (params.length > 1 && !params[1].isEmpty()) {
                try {
                    rounds = Integer.parseInt(params[1].replaceAll("[^0-9\\-]", ""));
                } catch (NumberFormatException ex) {
                    LOGGER.info("Error parsing injure command: " + ex.getMessage());
                    cmd.invalidate("error parsing duration");
                }
            }
            target.addData(new PlayerEvent("Injured", round, cmd.getPost().getUrl()));
            target.injure(rounds);
        }
    }

    private boolean votingOpen(DateTime timeStamp) {
        if (roundLength > 0 && lynchType == 1 || lynchType == 2)
            return getEndTime().isAfter(timeStamp);
        //        if (percentMajority < 100 && lynchType == 3 || lynchType == 4)
        //            // Return true if not enough players have voted for the same player.
        //            return votes.getHighestVotedCount() < getVotesForLynch(timeStamp);
        return true;
    }

    private DateTime getEndTime() {
        return startTime.plusHours(roundLength);
    }

    private int getVotesForLynch(DateTime timeStamp) {
        double players = livingPlayerCount();
        double votesRequired = players * percentMajority / 100.0;
        if (lynchType == 4)
            votesRequired -= new Duration(startTime, timeStamp).toStandardHours().getHours() / roundLength;
        return (int) Math.ceil(votesRequired);
    }
}
