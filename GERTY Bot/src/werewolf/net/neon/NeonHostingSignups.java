package werewolf.net.neon;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import werewolf.Utils;
import werewolf.net.Command;
import werewolf.net.ForumPost;
import werewolf.net.ForumThread;
import werewolf.net.ForumUser;
import werewolf.net.HostingSignups;

public class NeonHostingSignups implements HostingSignups
{
	private static class SignupRecord
	{
		protected ForumUser		host		= null;
		protected String		cohostName	= null;
		protected ForumUser		cohost		= null;
		protected ForumThread	thread		= null;
		protected String		name		= "";
		protected String		type		= "";
		protected int			gameNumber	= -1;
	}

	public static final int SIGNUP_THREAD = 274282;

	public static final NeonHostingSignups	INSTANCE		= new NeonHostingSignups();
	private NeonThread						thread			= NeonThread.getThread(NeonHostingSignups.SIGNUP_THREAD);
	private LinkedList<SignupRecord>		werewolfGames	= new LinkedList<SignupRecord>();
	private LinkedList<SignupRecord>		assassinsGames	= new LinkedList<SignupRecord>();
	private LinkedList<SignupRecord>		mafiaGames		= new LinkedList<SignupRecord>();
	private int								mafiaCount		= 0;																												// First
														// running/queued
														// mafia
														// game
														// number.
	private int								assassinsCount	= 0;																												// ...
														// assassins
														// game
														// number.

	private int werewolfCount = 0;														// ...
														// werewolf
														// game
														// number.

	private NeonHostingSignups()
	{

	}

	/*
	 * Commands: signup <type>[, <name>] cohost <cohost> confirm [<host>] name
	 * <name> thread <thread id> withdraw notify [<type>]
	 */
	private void checkSignupThread() throws IOException
	{
		this.thread.refresh();
		ForumPost post = this.thread.nextPost();
		while (post != null)
		{ // Parse any new commands.
			for (Command command : post.getCommands())
			{
				String cmd = command.getCommand().toLowerCase();
				// Search through all possible commands and execute any valid
				// ones.
				if (cmd.matches("^(signup)$"))
					this.signupForGame(command);
				if (cmd.matches("^(cohost)$"))
					this.setCohost(command);
				if (cmd.matches("^(confirm)$"))
					this.confirmForCohost(command);
				if (cmd.matches("^(name)$"))
					this.nameGame(command);
				if (cmd.matches("^(thread)$"))
					this.setThreadId(command);
				if (cmd.matches("^(delete|withdraw|remove|unsignup)$"))
					this.deleteSignupRecord(command);
			}
			post = this.thread.nextPost();
		}
	}

	@Override
	public void checkThreads()
	{

	}

	private void confirmForCohost(Command cmd)
	{

	}

	private void deleteSignupRecord(Command cmd)
	{

	}

	@Override
	public void endGame(String threadId)
	{
		this.parseProcessedSignups();

		// Check Werewolf queue:
		Iterator<SignupRecord> iter = this.werewolfGames.iterator();
		while (iter.hasNext())
		{
			SignupRecord game = iter.next();
			if (!game.thread.getThreadId().equals(threadId))
				continue;
			iter.remove();
			SignupRecord newGame = new SignupRecord();

			// Need to add a new game. Check to see if the new game needs to be
			// Werewolf or Assassins.
			if (this.werewolfGames.getLast().type.equalsIgnoreCase("Assassins") || this.werewolfGames.get(this.werewolfGames.size() - 2).type.equalsIgnoreCase("Assassins"))
			{
				newGame.type = "Assassins";
				newGame.gameNumber = ++this.assassinsCount;
			} else
			{
				newGame.type = "Werewolf";
				newGame.gameNumber = ++this.werewolfCount;
			}
		}

		// Check Mafia queue:
		iter = this.mafiaGames.iterator();
		while (iter.hasNext())
		{
			SignupRecord game = iter.next();
			if (!game.thread.getThreadId().equals(threadId))
				continue;
			iter.remove();
			SignupRecord newGame = new SignupRecord();

			if (this.mafiaGames.getLast().type.equalsIgnoreCase("Assassins") || this.mafiaGames.get(this.mafiaGames.size() - 2).type.equalsIgnoreCase("Assassins"))
			{
				newGame.type = "Assassins";
				newGame.gameNumber = ++this.assassinsCount;
			} else
			{
				newGame.type = "Mafia";
				newGame.gameNumber = ++this.mafiaCount;
			}
		}
	}

	@Override
	public ForumThread getThread()
	{
		return this.thread;
	}

	private void nameGame(Command cmd)
	{

	}

	// Possible syntax: <type> <number>: OPEN
	// <type> <number> by <host>[ and <cohost>]
	// <type> <number>: <title> by <host?[ and <cohost>]
	private SignupRecord parseGame(String gameData)
	{
		SignupRecord game = new SignupRecord();
		game.type = gameData.replaceFirst(" .*", "");
		gameData = gameData.replace(game.type + " ", "");
		game.gameNumber = Utils.RomanNumerals.decode(gameData.replaceFirst("[ \\:].*", ""));
		if (gameData.contains("OPEN"))
			return game;
		String host = gameData.replaceFirst(".* by ", "");
		if (host.contains(" and "))
		{
			String[] hosts = host.split(" and ");
			game.host = ForumUser.getUserFor(hosts[0], NeonContext.INSTANCE);
			game.cohostName = hosts[1];
			game.cohost = ForumUser.getUserFor(hosts[1], NeonContext.INSTANCE);
		} else
			game.host = ForumUser.getUserFor(host, NeonContext.INSTANCE);
		if (gameData.contains(":"))
		{
		}
		return game;
	}

	private void parseProcessedSignups()
	{
		try
		{
			String[] text = this.thread.getPosts().get(0).getRawText().split("\n");
			for (String game : text)
			{
				if (game.startsWith("Werewolf"))
					this.werewolfGames.add(this.parseGame(game));
				if (game.startsWith("Assassins"))
					this.assassinsGames.add(this.parseGame(game));
				if (game.startsWith("Mafia"))
					this.mafiaGames.add(this.parseGame(game));
			}
		} catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}

	@Override
	public void reset() throws IOException
	{
		this.thread.reset();
	}

	private void setCohost(Command cmd)
	{

	}

	private void setThreadId(Command cmd)
	{

	}

	private void signupForGame(Command cmd)
	{

	}

	@Override
	public boolean update() throws IOException
	{
		return false;
	}
}
