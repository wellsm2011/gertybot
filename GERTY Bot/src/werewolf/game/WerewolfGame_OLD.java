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

import com.sun.media.jfxmedia.events.PlayerEvent;

import werewolf.net.ParsedCommand;
import werewolf.net.ForumContext;
import werewolf.net.ForumPost;
import werewolf.net.ForumThread;
import werewolf.net.ForumUser;
import werewolf.net.ThreadManager;
import werewolf.net.neon.NeonContext;

public class WerewolfGame_OLD implements ThreadManager
{
	private static final Logger LOGGER = Logger.getLogger(WerewolfGame_OLD.class.getName());

	private static boolean isMe(ForumUser user)
	{
		return user.equals(user.getContext().LOGIN_USER);
	}

	private ForumThread			thread;
	private ArrayList<Player>	players		= new ArrayList<>();
	private VoteManager_OLD		votes		= new VoteManager_OLD(this.players);
	private ForumUser			host		= null;
	private ForumUser			cohost		= null;
	private StringBuilder		pastVotes	= new StringBuilder();

	private String[]	gameId;
	private int			round					= 0;
	private boolean		isDay					= false;
	private boolean		openSignups				= false;
	private boolean		hasStarted				= false;
	private boolean		multipleVotes			= false;
	private boolean		containsHostCommands	= false;

	private boolean resetVotesOnRoundChange = true;

	private String storyPosts = "";
	// 0: Undefined, 1: Timed, 2: Single, 3: Majority, 4: Decremental
	private byte	lynchType		= 0;
	private int		roundLength		= 0;
	private int		percentMajority	= 0;

	private DateTime	startTime	= DateTime.now();
	private boolean		stateChange	= false;
	private ForumPost	lastPost	= null;

	private boolean votesReset = false;

	/**
	 * Creates a new Werewolf game. Requires a ForumThread to use as a base.
	 *
	 * @param init
	 *            The forum thread that this game is played in.
	 * @throws IOException
	 *             if any of the underlying network operations throw an
	 *             exception.
	 */
	public WerewolfGame_OLD(ForumThread init) throws IOException
	{
		this.thread = init;
		this.host = this.thread.getPosts().get(0).getPoster();
		this.gameId = GameID.get(init.getThreadId());

		// Evaluate all posts for game commands.
		ForumPost post = this.thread.nextPost();
		while (post != null)
		{
			this.parseCommands(post);
			post = this.thread.nextPost();
		}
		WerewolfGame_OLD.LOGGER.info("Game initalized: " + this.thread);
	}

	private void addAlias(ParsedCommand cmd)
	{
		String[] params = cmd.getParamString().split(", ?");
		if (params.length < 2)
		{
			cmd.invalidate("not enough params");
			return;
		}

		ForumUser aliasPlayer;
		try
		{
			aliasPlayer = ForumUser.getUserFor(Integer.parseInt(params[0]), this.thread.getContext());
			if (aliasPlayer == null)
			{
				cmd.invalidate("unknown user, " + params[0]);
				return;
			}
		} catch (NumberFormatException ex)
		{
			cmd.invalidate("syntax: alias <id>, <alias>");
			return;
		}

		aliasPlayer.addAlias(params[1]);
	}

	private void addToGame(ParsedCommand cmd)
	{
		String[] params = cmd.getParamString().split(", ?");
		ForumUser newPlayer = this.getUser(params[0]);
		try
		{
			if (newPlayer == null && params.length > 1)
			{
				int userID = Integer.parseInt(params[1].replaceAll("[^0-9]", ""));
				newPlayer = ForumUser.getUserFor(userID, this.thread.getContext());
				if (newPlayer == null)
					newPlayer = ForumUser.getUserFor(userID, params[0], NeonContext.INSTANCE);
			}
		} catch (NumberFormatException ex)
		{
			WerewolfGame_OLD.LOGGER.info("Error parsing add command: " + ex.getMessage());
			cmd.invalidate("syntax error: [add name, id]");
		}
		if (!this.isHost(cmd.getUser()))
		{
			cmd.invalidate("invalid access");
			return;
		}
		if (newPlayer == null)
			return;
		for (Player plr : this.players)
			if (plr.equals(newPlayer))
			{
				cmd.invalidate("duplicate player");
				return;
			}
		this.players.add(new Player(newPlayer, cmd.getPost(), this.round));
		this.stateChange = true;
	}

	/**
	 * Changes the lynch manager for the game. By default, the bot does not
	 * participate in lynch tracking beyond votes.
	 * <P>
	 * Accepted types:<BR>
	 * <B>timed [hours]</B> - Causes the bot to stop accepting votes a given
	 * number of hours after each [dawn] post. Default: 48 hours.<BR>
	 * <B>single [hours]</B> - Causes the bot to not accept vote changes, in
	 * addition to the timed lynch above. Default: No Timelimit<BR>
	 * <B>majority [percent]</B> - Causes the bot to stop accepting votes once a
	 * majority of players have voted for the same target. Default: 100% (track
	 * only; don't disable votes).<BR>
	 * <B>decremental [hours [percent]]</B> - Causes the bot to track the
	 * current number of votes required for lynch, in addition to majority lynch
	 * above. Default: 24 hours, 100% (track only; don't disable votes).
	 *
	 * @param cmd
	 *            The command object that is attempting to call the command.
	 */
	private void changeLynchType(ParsedCommand cmd)
	{
		if (!this.isHost(cmd.getUser()))
		{
			cmd.invalidate("invalid access");
			return;
		}
		if (cmd.getPost().getPostTime() == null)
		{
			cmd.invalidate("error: cannot parse timestamps");
			return;
		}
		String[] params = cmd.getParamString().split(" ", 2);
		// Syntax: <type>[<options>]
		if (params[0].matches("timed"))
		{
			if (params.length == 1)
				this.roundLength = 48;
			else
				try
				{
					this.roundLength = Integer.parseInt(params[1]);
				} catch (NumberFormatException ex)
				{
					cmd.invalidate("cannot parse hours: " + params[1]);
					return;
				}
			this.lynchType = 1;
			// Set lynch type to timed.
		} else if (params[0].matches("single"))
		{
			if (params.length == 1)
				this.roundLength = 48;
			else
				try
				{
					this.roundLength = Integer.parseInt(params[1]);
				} catch (NumberFormatException ex)
				{
					cmd.invalidate("cannot parse hours: " + params[1]);
					return;
				}
			this.lynchType = 2;
			// Set lynch type to single vote.
		} else if (params[0].matches("majority"))
		{
			if (params.length == 1)
				this.percentMajority = 100;
			else
				try
				{
					this.percentMajority = Integer.parseInt(params[1].replace("%", ""));
				} catch (NumberFormatException ex)
				{
					cmd.invalidate("cannot parse percent: " + params[1]);
					return;
				}
			this.lynchType = 3; // Set lynch type to majority.
		} else if (params[0].matches("decremental"))
		{
			if (params.length < 3)
				this.percentMajority = 100;
			else
				try
				{
					this.percentMajority = Integer.parseInt(params[2].replace("%", ""));
				} catch (NumberFormatException ex)
				{
					cmd.invalidate("cannot parse percent: " + params[2]);
					return;
				}
			if (params.length < 2)
				this.roundLength = 48;
			else
				try
				{
					this.roundLength = Integer.parseInt(params[1]);
				} catch (NumberFormatException ex)
				{
					cmd.invalidate("cannot parse hours: " + params[1]);
					return;
				}
			this.lynchType = 4; // Set lynch type to decremental.
		} else
			cmd.invalidate("unknown type: " + params[0]);
	}

	private void changeSignups(ParsedCommand cmd)
	{
		if (!this.isHost(cmd.getUser()))
			cmd.invalidate("invalid access");
		else if (cmd.getParamString().toLowerCase().contains("open"))
			this.openSignups = true;
		else if (cmd.getParamString().toLowerCase().contains("closed"))
			this.openSignups = false;
		else
			cmd.invalidate("incomplete command");
	}

	private void checkVoteReset()
	{
		if (this.votesReset)
			return;

		for (Player plr : this.players)
		{ // Record each player's final vote.
			LinkedList<Vote> plrVotes = this.votes.getVotes(plr);
			if (plr.isInjured() || !plr.isAlive())
				plr.endRound(new Vote(plr, StaticUser.INCAPACITATED));
			else if (!plrVotes.isEmpty())
				plr.endRound(plrVotes.getLast());
			else
				plr.endRound(new Vote(plr, StaticUser.NOVOTE));
		}
		if (this.round > 0 && this.resetVotesOnRoundChange)
		{
			if (this.pastVotes.length() > 0)
				this.pastVotes.insert(0, "\n--------------------\n\n");
			this.pastVotes.insert(0, this.thread.getContext().header("R" + this.round) + " " + this.getVoteString());
			this.votes.reset();
		}
		this.votesReset = true;
	}

	private void dawnPost(ParsedCommand cmd)
	{
		if (!this.isHost(cmd.getUser()))
		{
			cmd.invalidate("invalid access");
			return;
		}
		if (!this.hasStarted)
			this.startGame(cmd);
		this.checkVoteReset();
		this.votesReset = false;
		this.isDay = true;
		this.round += 1;
	}

	private void duskPost(ParsedCommand cmd)
	{
		if (!this.isHost(cmd.getUser()))
		{
			cmd.invalidate("invalid access");
			return;
		}
		if (!this.hasStarted)
			this.dawnPost(cmd);
		this.openSignups = false;
		this.hasStarted = true;
		this.isDay = false;
		this.checkVoteReset();
	}

	private void endGame(ParsedCommand cmd)
	{
		try
		{
			if (!this.isHost(cmd.getUser()))
			{
				cmd.invalidate("invalid access");
				return;
			}

			String[] params = cmd.getParamString().split(", ?");
			LinkedList<ForumUser> winners = new LinkedList<>();
			LinkedList<ForumUser> losers = new LinkedList<>();
			LinkedList<ForumUser> hosts = new LinkedList<>();

			for (String plr : params)
				winners.add(this.getPlayer(plr));

			if (winners.size() > 0)
			{
				losers.addAll(this.players);
				losers.removeAll(winners);
				hosts.add(this.host);
				if (this.cohost != null)
					hosts.add(this.cohost);

				try
				{
					this.thread.getContext().getRecord().addGame(this.thread, hosts, winners, losers);
				} catch (NullPointerException e)
				{
					WerewolfGame_OLD.LOGGER.log(Level.INFO, "Invalid context to record results: " + this.thread.getContext().toString() + ".", e);
				}

				try
				{
					// thread.getContext().SIGNUPS.endGame(thread.getThreadId());
				} catch (NullPointerException e)
				{
					WerewolfGame_OLD.LOGGER.log(Level.INFO, "Invalid context to complete signups: " + this.thread.getContext().toString() + ".", e);
				}
			}
		} catch (IOException | NullPointerException e)
		{
			WerewolfGame_OLD.LOGGER.log(Level.WARNING, "Unable to complete end command in " + this.thread.toString() + ".", e);
		}
	}

	/**
	 * @return The user marked as cohost by the host, or null if no cohost
	 *         exists.
	 */
	public ForumUser getCohost()
	{
		return this.cohost;
	}

	private DateTime getEndTime()
	{
		return this.startTime.plusHours(this.roundLength);
	}

	private String getFinalVoteHistory()
	{
		StringBuilder output = new StringBuilder();

		for (Player plr : this.players)
		{
			output.append(plr.getVotes());
			output.append("\n");
		}

		return output.toString();
	}

	/**
	 * @return The user marked as host for this round. (eg, the first poster in
	 *         the thread)
	 */
	public ForumUser getHost()
	{
		return this.host;
	}

	private LinkedList<Player> getInactivePosters()
	{
		LinkedList<Player> output = new LinkedList<>();
		for (Player plr : this.players)
			if (plr.inactivePostRounds() > 1 && plr.isAlive())
				output.add(plr);
		return output;
	}

	/**
	 * @return A string list of all the inactive players in the game.
	 */
	private String getInactives()
	{
		StringBuilder output = new StringBuilder();
		LinkedList<Player> inactivePosters = this.getInactivePosters();
		LinkedList<Player> inactiveVoters = this.getInactiveVoters();

		// TODO: Figure out how to format the inavtives list in a more readable
		// way.
		if (!inactivePosters.isEmpty())
		{
			output.append("Inactive Posters: ");
			boolean isFirst = true;
			for (Player plr : inactivePosters)
			{
				if (!isFirst)
					output.append(", ");
				output.append(plr.getName() + " (" + plr.inactivePostRounds() + " rounds)");
				isFirst = false;
			}
			if (!inactiveVoters.isEmpty())
				output.append("\n");
		}

		if (!inactiveVoters.isEmpty())
		{
			output.append("Inactive Voters: ");
			boolean isFirst = true;
			for (Player plr : inactiveVoters)
			{
				if (!isFirst)
					output.append(", ");
				output.append(plr.getName() + " (" + plr.inactiveVoteRounds() + " rounds)");
				isFirst = false;
			}
		}

		return output.toString();
	}

	private LinkedList<Player> getInactiveVoters()
	{
		LinkedList<Player> output = new LinkedList<>();
		for (Player plr : this.players)
			if (plr.inactiveVoteRounds() > 1 && plr.isAlive())
				output.add(plr);
		return output;
	}

	private ForumUser getObject(String name, Collection<? extends ForumUser> list)
	{
		for (ForumUser user : list)
			if (user.getName().equalsIgnoreCase(name))
				return user;

		ForumUser found = null;
		for (ForumUser user : list)
			if (user.getName().toLowerCase().startsWith(name.toLowerCase()))
			{
				if (found != null)
					return null;
				found = user;
			}
		if (found != null)
			return found;

		for (ForumUser user : list)
			for (String alias : user.getAliases())
				if (alias.toLowerCase().contains(name.toLowerCase()))
				{
					if (found != null && found != user)
						return null;
					found = user;
				}
		if (found != null)
			return found;

		for (ForumUser user : list)
			if (user.getName().toLowerCase().contains(name.toLowerCase()))
			{
				if (found != null)
					return null;
				found = user;
			}

		return found;
	}

	private ForumUser getObjectById(int id, List<? extends ForumUser> list)
	{
		for (ForumUser user : list)
			if (user.getUserId() == id)
				return user;
		return null;
	}

	/**
	 * @return The current phase of the game, such as "R1 Day" or "R5 Night" or
	 *         "Pregame Setup"
	 */
	private String getPhaseString()
	{
		if (!this.hasStarted)
			return "Phase: Pregame Setup";
		if (this.isDay)
			return "Phase: R" + this.round + " Day";
		return "Phase: R" + this.round + " Night";
	}

	/**
	 * @param name
	 *            The name, either partial or whole, of the player to search
	 *            for.
	 * @return The player object represented by the input string, or null if no
	 *         player was found or if multiple players matched the given name.
	 */
	public Player getPlayer(String name)
	{
		return (Player) this.getObject(name, this.players);
	}

	public Player getPlayerById(int id)
	{
		return (Player) this.getObjectById(id, this.players);
	}

	/**
	 * @return A list of all current players, along with all data about each
	 *         player so far this game.
	 */
	private String getPlayerStatus()
	{
		StringBuilder output = new StringBuilder();
		if (this.players.isEmpty())
			return "None.";
		for (int i = 0; i < this.players.size(); ++i)
		{
			Player plr = this.players.get(i);
			String name = plr.getName();
			if (!plr.isAlive())
				name = "[color=#FFBF80]" + this.thread.getContext().strike(name) + "[/color]";
			// eg, 02: Bob - Lynched R4, Shot R5
			ForumPost joinPost = plr.getJoinPost();
			String url = plr.getContext().getPostUrl(joinPost.getThreadId(), joinPost.getPage(), joinPost.getPostId());
			output.append(String.format("[url=%s]%02d[/url]: %s %s%n", url, i + 1, name, plr.getForumData()));
		}
		return output.toString();
	}

	/**
	 * @return The thread in which this game takes place.
	 */
	@Override
	public ForumThread getThread()
	{
		return this.thread;
	}

	private ForumUser getUser(String name)
	{
		return this.getObject(name, this.thread.getContext().USERS.getKnownUsers());
	}

	private int getVotesForLynch(DateTime timeStamp)
	{
		double players = this.livingPlayerCount();
		double votesRequired = players * this.percentMajority / 100.0;
		if (this.lynchType == 4)
			votesRequired -= new Duration(this.startTime, timeStamp).toStandardHours().getHours() / this.roundLength;
		return (int) Math.ceil(votesRequired);
	}

	/**
	 * @return A list of the current tally and reverse tally for this round.
	 *         Includes a list of players not voting.
	 */
	private String getVoteString()
	{
		ForumContext context = this.thread.getContext();
		String voteString = context.header("Lynch Tally") + "\n" + this.votes.getTally();
		if (context.allowExpectedLynch() && this.lynchType < 3)
			voteString += "\nExpected LHLV Lynch: [b]" + this.votes.getExpectedLhlvLynch().getName() + "[/b]";
		// 0: Undefined, 1: Timed, 2: Single, 3: Majority, 4: Decremental.
		switch (this.lynchType)
		{
			case 1:
			case 2:
				if (this.roundLength > 0)
				{
					voteString += "\nLynch will end on " + this.getEndTime().toString("E 'at' hh:mm a z.");
					if (!this.votingOpen(DateTime.now()))
						voteString += " [color=#FF0000](Time expired)[/color]";
				}
				break;
			case 3:
			case 4:
				voteString += "\n" + this.getVotesForLynch(DateTime.now()) + " votes required for lynch.";
				if (this.lynchType == 4)
				{
					DateTime nextReduction = this.startTime.plusHours(this.roundLength);
					while (nextReduction.isBefore(DateTime.now()))
						nextReduction = nextReduction.plusHours(this.roundLength);
					voteString += " This number will go down by one on " + nextReduction.toString("E 'at' hh:mm a z.");
				}
				break;
		}
		voteString += "\nTotal: " + this.livingPlayerCount() + " living players.\n";
		if (this.votes.length() > 0 && this.lynchType != 2)
			voteString += "\n\n" + context.header("Reverse Tally") + "\n" + this.votes.getReverseTally();
		return voteString + "\n";
	}

	private boolean hasLastPost()
	{
		return this.lastPost != null && !this.lastPost.hasBeenDeleted();
	}

	private void injurePlayer(ParsedCommand cmd)
	{
		String[] params = cmd.getParamString().split(", ?", 2);
		Player target = this.getPlayer(params[0]);
		if (!this.isHost(cmd.getUser()))
			cmd.invalidate("invalid access");
		else if (target == null)
			cmd.invalidate("unknown player");
		else
		{
			int rounds = 1;
			if (params.length > 1 && !params[1].isEmpty())
				try
				{
					rounds = Integer.parseInt(params[1].replaceAll("[^0-9\\-]", ""));
				} catch (NumberFormatException ex)
				{
					WerewolfGame_OLD.LOGGER.info("Error parsing injure command: " + ex.getMessage());
					cmd.invalidate("error parsing duration");
				}
			target.logEvent(new PlayerEvent("Injured", this.round, cmd.getPost().getUrl()));
			target.injure(rounds);
		}
	}

	/**
	 * @param user
	 *            The user to check.
	 * @return True if the given player is the host, cohost or the bot itself;
	 *         false otherwise.
	 */
	public boolean isHost(ForumUser user)
	{
		return user != null && (user.equals(this.host) || user.equals(this.cohost) || WerewolfGame_OLD.isMe(user));
	}

	private void joinGame(ParsedCommand cmd)
	{
		if (!this.openSignups)
		{
			cmd.invalidate("signups closed");
			return;
		}

		for (Player plr : this.players)
			if (plr.equals(cmd.getUser()))
			{
				cmd.invalidate("duplicate player");
				return;
			}

		this.players.add(new Player(cmd.getUser(), cmd.getPost(), this.round));
		this.stateChange = true;
	}

	private void killPlayer(ParsedCommand cmd)
	{
		if (!this.isHost(cmd.getUser()))
		{
			cmd.invalidate("invalid access");
			return;
		}
		String[] params = cmd.getParamString().split(", ?", 2); // Syntax:
		// <target>[,
		// <message>]
		Player target = this.getPlayer(params[0]);
		if (target != null)
		{
			String message = "Killed";
			if (params.length > 1)
				message = params[1];

			target.kill(new PlayerEvent(message, this.round, cmd.getPost().getUrl()));
			this.stateChange = true;
		}
	}

	private void leaveGame(ParsedCommand cmd)
	{
		if (this.players.remove(cmd.getUser()))
			this.stateChange = true;
		else
			cmd.invalidate("unknown player");
	}

	/**
	 * @return The number of players in the game who are considered alive.
	 */
	private int livingPlayerCount()
	{
		int count = 0;
		for (Player plr : this.players)
			if (plr.isAlive())
				count++;
		return count;
	}

	private void logPlayerData(ParsedCommand cmd)
	{
		if (!this.isHost(cmd.getUser()))
		{
			cmd.invalidate("invalid access");
			return;
		}
		// Command comes in the form "log <player>, <reason>"
		String[] params = cmd.getParamString().split(", ?", 2);
		Player target = this.getPlayer(params[0]);

		if (params.length < 2)
			cmd.invalidate("missing params");
		else if (target == null)
			cmd.invalidate("unknown player");
		else
			target.logEvent(new PlayerEvent(params[1].trim(), this.round, cmd.getPost().getUrl()));
	}

	private void makeVote(ParsedCommand cmd)
	{
		if (!this.isDay)
		{
			cmd.invalidate("not day");
			return;
		}
		try
		{
			Player voter = this.getPlayerById(cmd.getUser().getUserId());
			User voted = this.getPlayer(cmd.getParamString().replaceAll("^ *\\- *", ""));

			if (cmd.getParamString().toLowerCase().matches("no[ \\-]?lynch"))
				voted = StaticUser.NOLYNCH;
			if (cmd.getParamString().toLowerCase().matches("no[ \\-]?king"))
				voted = StaticUser.NOKING;
			if (voted == null)
				cmd.invalidate("unknown target");
			else if (voter == null)
				cmd.invalidate("unknown voter");
			else if (voter.isInjured())
				/*
				 * Injured players are not allowed to vote.
				 */
				cmd.invalidate("injured voter");
			else if (!voter.isAlive())
				cmd.invalidate("voter dead");
			else if (voted instanceof Player && !((Player) voted).isAlive())
				cmd.invalidate("target dead");
			else if (cmd.getPost().getPostTime() != null && !this.votingOpen(cmd.getPost().getPostTime()))
				cmd.invalidate("voting has ended");
			else if (this.lynchType == 2 && this.votes.getVotes(voter).size() > 0)
				cmd.invalidate("can't change vote");
			else
			{
				this.votes.placeVote(new Vote(voter, voted, cmd.getPost()));
				this.stateChange = true;
			}
		} catch (IndexOutOfBoundsException ex)
		{
			cmd.invalidate("unknown player");
		}
	}

	private void modkillPlayer(ParsedCommand cmd)
	{
		if (!this.isHost(cmd.getUser()))
		{
			cmd.invalidate("invalid access");
			return;
		}
		String[] params = cmd.getParamString().split(", ?", 2); // Syntax:
		// <target>[,
		// <message>]
		Player target = this.getPlayer(params[0]);
		if (target != null)
		{
			String message = "Modkilled";
			if (params.length > 1)
			{
				message = params[1];
				if (!params[1].trim().toLowerCase().startsWith("modkilled for"))
					message = "Modkilled for " + message;
			}

			target.kill(new PlayerEvent(message, this.round, cmd.getPost().getUrl()));
			this.stateChange = true;
		}
	}

	private void parseCommands(ForumPost post) throws IOException
	{
		boolean containsCommand = false;
		for (Player plr : this.players)
			if (plr.equals(post.getPoster()))
				plr.madePost();
		for (ParsedCommand command : post.getCommands())
		{
			command.startCheck();
			boolean isCommand = true;
			String cmd = command.getCommand().toLowerCase();
			// Search through all possible commands and execute any valid ones.
			if (cmd.matches("^(join|in)$"))
				/*
				 * Adds the user to the game. Requires signups open and pregame
				 * setup.
				 */
				this.joinGame(command);
			else if (cmd.matches("^(leave|quit|out)$"))
				/*
				 * Removes the user from the game.
				 */
				this.leaveGame(command);
			else if (cmd.matches("^(add)$"))
				/*
				 * Forces a user to join the game. Only usable by the host.
				 * Requires the forced user to have posted at least once in the
				 * game thread.
				 */
				this.addToGame(command);
			else if (cmd.matches("^(replace)$"))
				/*
				 * Replaces a user with another user. Only usable by the host.
				 */
				this.replaceInGame(command);
			else if (cmd.matches("^(alias)$"))
				this.addAlias(command);
			else if (cmd.matches("^(remove)$"))
				/*
				 * Removes a player from the game completely. Only usable by the
				 * host.
				 */
				this.removeFromGame(command);
			else if (cmd.matches("^(vote|lynch|banish)$"))
				/*
				 * Logs the user as voting for another player. Requires day and
				 * can only be used by a player.
				 */
				this.makeVote(command);
			else if (cmd.matches("^(unvote|abstain)$"))
				/*
				 * Logs the user as not voting. Requires day and can only be
				 * used by a player.
				 */
				this.removeVote(command);
			else if (cmd.matches("^(sign\\-?ups?)$"))
				/*
				 * Marks signups as open or closed. May only be used by the
				 * host.
				 */
				this.changeSignups(command);
			else if (cmd.matches("^(dusk|night)$"))
				/*
				 * Marks that the game is now in night phase, jumping from the
				 * current phase forward to night. Only usable by the host.
				 */
				this.duskPost(command);

			else if (cmd.matches("^(dawn|day)$"))
				/*
				 * Marks that the game is now in day phase, jumping from the
				 * current phase forward to day. Only usable by the host.
				 */
				this.dawnPost(command);
			else if (cmd.matches("^(start)$"))
				/*
				 * Starts the game and puts it into round zero. Automatically
				 * closes signups. Only usable by the host.
				 */
				this.startGame(command);
			else if (cmd.matches("^(kill)$"))
				/*
				 * Marks a player as killed and removes them from the game.
				 * Unlike remove, keeps their data. Only usable by host.
				 */
				this.killPlayer(command);
			else if (cmd.matches("^(modkill)$"))
				/*
				 * Marks a player as modkilled and removes them from the game.
				 * Unlike remove, keeps their data. Only usable by host.
				 */
				this.modkillPlayer(command);
			else if (cmd.matches("^(revive|raise)$"))
				/*
				 * Marks a previously killed player as alive again. Only usable
				 * by the host.
				 */
				this.revivePlayer(command);
			else if (cmd.matches("^(log|data|note)$"))
				/*
				 * Logs some extra data about a player, but does not change
				 * their status in any way. Only usable by the host.
				 */
				this.logPlayerData(command);
			else if (cmd.matches("^(injure|hospitalize)$"))
				/*
				 * Logs a player as having been injured and unable to vote. Bars
				 * the player from voting in the next round. Host only.
				 */
				this.injurePlayer(command);
			else if (cmd.matches("^(co\\-?host)$"))
				/*
				 * Sets a player as the cohost of the game, giving them all the
				 * powers the host has. Only usable by the host.
				 */
				this.setCohost(command);
			else if (cmd.matches("^(vote|lynch)(ing)?type$"))
				/**
				 * Sets the current lynch type. Supported types:
				 * <ul>
				 * <li>Majority [<%>]</li>
				 * <li>Timed <hours></li>
				 * <li>Single [<hours>]</li>
				 * <li>Decremantal <hours> [<%>]</li>
				 * </ul>
				 */
				this.changeLynchType(command);
			else if (cmd.matches("^(end)$"))
				/*
				 * Marks a game as being complete. May be used with a list of
				 * winning players to have the bot update win/loss records. Only
				 * usable by the host.
				 */
				this.endGame(command);
			else if (cmd.matches("^(story(post)?|flag|title)$"))
				/*
				 * Marks a post as being importiant. Optionally adds a title to
				 * the post. Only usable by the host.
				 */
				this.setStoryPost(command);
			else if (cmd.matches("^(re(hash|start|set))$"))
				try
				{
					if (this.hasLastPost())
						this.lastPost.delete();
					if (post.getCommands().size() > 1)
						command.invalidate("rehash complete");
					else
						post.delete();
					this.reset();
					break;
				} catch (IOException ex)
				{
					WerewolfGame_OLD.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
				}
			else
			{ // Unknown command found.
				isCommand = false;
				command.invalidate("unknown command");
			}

			if (isCommand && command.isMarkedHidden() && this.isHost(command.getUser()))
				/*
				 * 'Hidden' commands are commands with two brackets instead of
				 * one. eg, [[command]]
				 */
				command.hide();
			if (isCommand && command.isChecking())
				/*
				 * Means this command was previously invalid, but has since
				 * become valid.
				 */
				command.validate();

			containsCommand = containsCommand || isCommand;
		}
		if (post.getPoster().equals(this.host) && containsCommand)
			this.containsHostCommands = true;
		if (post.getPoster().equals(this.thread.getContext().LOGIN_USER) && !containsCommand)
		{
			this.lastPost = post;
			this.stateChange = false;
		}
	}

	private void removeFromGame(ParsedCommand cmd)
	{
		if (!this.isHost(cmd.getUser()))
		{
			cmd.invalidate("invalid access");
			return;
		}
		this.stateChange = this.players.remove(this.getPlayer(cmd.getParamString()));
	}

	private void removeVote(ParsedCommand cmd)
	{
		Player player = this.getPlayer(cmd.getUser().getName());

		if (player == null)
			cmd.invalidate("unknown player");
		else if (!this.isDay)
			cmd.invalidate("not day");
		else if (cmd.getPost().getPostTime() != null && !this.votingOpen(cmd.getPost().getPostTime()))
			cmd.invalidate("voting has ended");
		else if (this.lynchType == 2 && this.votes.getVotes(player).size() > 0)
			cmd.invalidate("can't change vote");
		else
		{
			if (player.isInjured())
				/*
				 * Injured players are not allowed to vote.
				 */
				return;

			this.votes.placeVote(new Vote(player, StaticUser.NOVOTE, cmd.getPost()));
			this.stateChange = true;
		}
	}

	private void replaceInGame(ParsedCommand cmd)
	{
		String[] params = cmd.getParamString().split(", ?");
		ForumUser newPlayer = this.getUser(params[1]);
		Player oldPlayer = this.getPlayer(params[0]);
		if (newPlayer == null || oldPlayer == null)
			return;
		for (Player plr : this.players)
			if (plr.equals(newPlayer))
			{
				cmd.invalidate("duplicate player");
				return;
			}

		Player plr = new Player(newPlayer, cmd.getPost(), this.round);
		plr.replacePlayer(oldPlayer);
		this.players.set(this.players.indexOf(oldPlayer), plr);
		this.stateChange = true;
	}

	/**
	 * Resets the entire game and parses the thread from scratch. Usually does
	 * not require the thread to be reloaded.
	 *
	 * @throws IOException
	 *             If any of the underlying network calls throw an error.
	 */
	@Override
	public void reset() throws IOException
	{
		this.thread.reset();
		this.players.clear();
		this.votes = new VoteManager_OLD(this.players);
		this.pastVotes = new StringBuilder();

		this.round = 0;
		this.isDay = false;
		this.openSignups = false;
		this.hasStarted = false;
		this.multipleVotes = false;
		this.resetVotesOnRoundChange = true;

		this.stateChange = false;
		this.lastPost = null;

		// Search through thread again to recreate game.
		ForumPost post = this.thread.nextPost();
		while (post != null)
		{
			this.parseCommands(post);
			post = this.thread.nextPost();
		}
	}

	private void revivePlayer(ParsedCommand cmd)
	{
		Player target = this.getPlayer(cmd.getParamString());

		if (!this.isHost(cmd.getUser()))
			cmd.invalidate("invalid access");
		else if (target == null)
			cmd.invalidate("unknown player");
		else if (target.isAlive())
			cmd.invalidate("target not dead");
		else
			this.stateChange |= target.revive(new PlayerEvent("Revived", this.round, cmd.getPost().getUrl()));
	}

	private void setCohost(ParsedCommand cmd)
	{
		if (!this.isHost(cmd.getUser()))
		{
			cmd.invalidate("invalid access");
			return;
		}
		String[] params = cmd.getParamString().split(", ?", 2); // Syntax:
		// <name>[,
		// <user ID>]
		ForumUser newCohost = this.getUser(params[0]);
		try
		{
			if (newCohost == null && params.length > 1)
			{
				int userID = Integer.parseInt(params[1].replaceAll("[^0-9]", ""));
				newCohost = ForumUser.getUserFor(userID, this.thread.getContext());
				if (newCohost == null)
					newCohost = ForumUser.getUserFor(userID, params[0], NeonContext.INSTANCE);
			}
		} catch (NumberFormatException ex)
		{
			WerewolfGame_OLD.LOGGER.info("Error parsing cohost command: " + ex.getMessage());
		}
		if (newCohost != null)
			this.cohost = newCohost;
		else
			cmd.invalidate("unknown user");
	}

	private void setStoryPost(ParsedCommand cmd)
	{
		if (!this.isHost(cmd.getUser()))
			cmd.invalidate("invalid access");
		else if (cmd.getParamString().length() < 1)
			this.storyPosts += "[url=" + cmd.getPost().getUrl() + "] Round " + this.round + "[/url]\n";
		else
			this.storyPosts += "[*][url=" + cmd.getPost().getUrl() + "]" + cmd.getParamString() + "[/url]\n";
	}

	private void startGame(ParsedCommand cmd)
	{
		if (!this.isHost(cmd.getUser()))
		{
			cmd.invalidate("invalid access");
			return;
		}
		this.hasStarted = true;
		this.openSignups = false;
	}

	/**
	 * Checks to see if the bot needs to post in the main thread. Also updates
	 * the bot's information on the game.
	 *
	 * @return True if the bot made a new post, false otherwise.
	 * @throws IOException
	 *             If any of the underlying network operations throw an error.
	 */
	@Override
	public boolean update() throws IOException
	{
		this.thread.refresh();
		ForumPost post = this.thread.nextPost();
		while (post != null)
		{ // Parse any new commands.
			this.parseCommands(post);
			post = this.thread.nextPost();
		}
		WerewolfGame_OLD.LOGGER.info("Game parsed: " + this.thread.getTitle());

		ForumContext context = this.thread.getContext();

		// Check to see if we need to post again...
		if (!this.containsHostCommands || this.hasLastPost() && !this.stateChange && this.lastPost.getPage() == this.thread.pages())
			return false;

		// Construct post.
		String postString = "";
		if (this.isDay && this.hasStarted)
			postString = this.getVoteString() + "\n" + this.getInactives();
		if (this.pastVotes.length() > 0)
			postString += "\n\n" + context.spoiler("Voting History", this.pastVotes.toString());
		if (this.round > 2)
			postString += "\n\n" + context.spoiler("Final Vote History", this.getFinalVoteHistory());
		if (postString.length() > 0 && this.isDay && this.hasStarted)
			postString += "\n\n" + context.spoiler("Players", this.getPlayerStatus());
		else
			postString = context.header("Players") + "\n" + this.getPlayerStatus() + "\n\n" + this.getInactives() + "\n\n" + postString;
		if (this.storyPosts.length() > 0 && this.hasStarted)
			postString += "\n\n" + context.header("Story Posts") + "[list]" + this.storyPosts + "[/list]\n";

		postString += "\n\n" + this.getPhaseString();
		if (context.RULES_URL != null)
			postString += "\n[url=" + context.RULES_URL + "]Rules Thread[/url]\n";
		if (context.allowPMs())
			postString += "\nGame ID: " + this.gameId[1] + " (" + this.gameId[0] + ")\n" + "[i]Use the game id or it's abbreviation as the subject of any PMs to the bot regarding this game.[/i]";
		try
		{
			if (this.hasLastPost())
				this.lastPost.delete();
		} catch (IOException ex)
		{
			WerewolfGame_OLD.LOGGER.warning("Cannot delete post in " + this.thread.getTitle() + ": " + ex.getMessage());
			ex.printStackTrace();
		}

		// Make post.
		this.thread.post(postString);
		this.stateChange = false;
		return true;
	}

	private boolean votingOpen(DateTime timeStamp)
	{
		if (this.roundLength > 0 && this.lynchType == 1 || this.lynchType == 2)
			return this.getEndTime().isAfter(timeStamp);
		// if (percentMajority < 100 && lynchType == 3 || lynchType == 4)
		// // Return true if not enough players have voted for the same player.
		// return votes.getHighestVotedCount() < getVotesForLynch(timeStamp);
		return true;
	}
}
