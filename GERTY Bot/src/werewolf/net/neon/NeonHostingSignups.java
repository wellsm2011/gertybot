package werewolf.net.neon;

import java.io.IOException;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;

import java.util.Set;

import java.util.stream.Collectors;
import java.util.stream.LongStream;

import werewolf.net.Command;
import werewolf.net.ForumPost;
import werewolf.net.ForumThread;
import werewolf.net.ForumUser;
import werewolf.net.HostingSignups;
import werewolf.net.Utils;

public class NeonHostingSignups implements HostingSignups {
    public static final int SIGNUP_THREAD = 274282;

    public static final NeonHostingSignups INSTANCE = new NeonHostingSignups();

    private NeonThread               thread = NeonThread.getThread(SIGNUP_THREAD);
    private LinkedList<SignupRecord> werewolfGames = new LinkedList<SignupRecord>();
    private LinkedList<SignupRecord> assassinsGames = new LinkedList<SignupRecord>();
    private LinkedList<SignupRecord> mafiaGames = new LinkedList<SignupRecord>();
    private int                      mafiaCount = 0;    //First running/queued mafia game number.
    private int                      assassinsCount = 0;    //  ... assassins game number.
    private int                      werewolfCount = 0;     //  ... werewolf game number.

    private NeonHostingSignups() {
        
    }

    private static class SignupRecord {
        protected ForumUser   host = null;
        protected String      cohostName = null;
        protected ForumUser   cohost = null;
        protected ForumThread thread = null;
        protected String      name = "";
        protected String      type = "";
        protected int         gameNumber = -1;
    }


    private void parseProcessedSignups() {
        try {
            String[] text = thread.getPosts().get(0).getRawText().split("\n");
            for (String game : text) {
                if (game.startsWith("Werewolf"))
                    werewolfGames.add(parseGame(game));
                if (game.startsWith("Assassins"))
                    assassinsGames.add(parseGame(game));
                if (game.startsWith("Mafia"))
                    mafiaGames.add(parseGame(game));
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    //Possible syntax: <type> <number>: OPEN
    //                 <type> <number> by <host>[ and <cohost>]
    //                 <type> <number>: <title> by <host?[ and <cohost>]
    private SignupRecord parseGame(String gameData) {
        SignupRecord game = new SignupRecord();
        game.type = gameData.replaceFirst(" .*", "");
        gameData = gameData.replace(game.type + " ", "");
        game.gameNumber = Utils.RomanNumerals.decode(gameData.replaceFirst("[ \\:].*", ""));
        if (gameData.contains("OPEN"))
            return game;
        String host = gameData.replaceFirst(".* by ", "");
        if (host.contains(" and ")) {
            String[] hosts = host.split(" and ");
            game.host = ForumUser.getUserFor(hosts[0], NeonContext.INSTANCE);
            game.cohostName = hosts[1];
            game.cohost = ForumUser.getUserFor(hosts[1], NeonContext.INSTANCE);
        }
        else
            game.host = ForumUser.getUserFor(host, NeonContext.INSTANCE);
        if (gameData.contains(":")) {
        }
        return game;
    }

    public boolean update() throws IOException {
    return false;
    }

    @Override
    public void endGame(String threadId) {
        parseProcessedSignups();

        // Check Werewolf queue:
        Iterator<SignupRecord> iter = werewolfGames.iterator();
        while (iter.hasNext()) {
            SignupRecord game = iter.next();
            if (!game.thread.getThreadId().equals(threadId))
                continue;
            iter.remove();
            SignupRecord newGame = new SignupRecord();

            // Need to add a new game. Check to see if the new game needs to be Werewolf or Assassins.
            if (werewolfGames.getLast().type.equalsIgnoreCase("Assassins") ||
                werewolfGames.get(werewolfGames.size() - 2).type.equalsIgnoreCase("Assassins")) {
                newGame.type = "Assassins";
                newGame.gameNumber = ++assassinsCount;
            } else {
                newGame.type = "Werewolf";
                newGame.gameNumber = ++werewolfCount;
            }
        }


        // Check Mafia queue:
        iter = mafiaGames.iterator();
        while (iter.hasNext()) {
            SignupRecord game = iter.next();
            if (!game.thread.getThreadId().equals(threadId))
                continue;
            iter.remove();
            SignupRecord newGame = new SignupRecord();

            if (mafiaGames.getLast().type.equalsIgnoreCase("Assassins") ||
                mafiaGames.get(mafiaGames.size() - 2).type.equalsIgnoreCase("Assassins")) {
                newGame.type = "Assassins";
                newGame.gameNumber = ++assassinsCount;
            } else {
                newGame.type = "Mafia";
                newGame.gameNumber = ++mafiaCount;
            }
        }
    }


    @Override
    public void checkThreads() {

    }

    /*
     * Commands:
     * signup <type>[, <name>]
     * cohost <cohost>
     * confirm [<host>]
     * name <name>
     * thread <thread id>
     * withdraw
     * notify [<type>]
     */
    private void checkSignupThread() throws IOException {
        thread.refresh();
        ForumPost post = thread.nextPost();
        while (post != null) { // Parse any new commands.
            for (Command command : post.getCommands()) {
                String cmd = command.getCommand().toLowerCase();
                // Search through all possible commands and execute any valid ones.
                if (cmd.matches("^(signup)$"))
                    signupForGame(command);
                if (cmd.matches("^(cohost)$"))
                    setCohost(command);
                if (cmd.matches("^(confirm)$"))
                    confirmForCohost(command);
                if (cmd.matches("^(name)$"))
                    nameGame(command);
                if (cmd.matches("^(thread)$"))
                    setThreadId(command);
                if (cmd.matches("^(delete|withdraw|remove|unsignup)$"))
                    deleteSignupRecord(command);
            }
            post = thread.nextPost();
        }
    }

    private void signupForGame(Command cmd) {

    }

    private void setCohost(Command cmd) {

    }

    private void confirmForCohost(Command cmd) {

    }

    private void nameGame(Command cmd) {

    }

    private void setThreadId(Command cmd) {

    }

    private void deleteSignupRecord(Command cmd) {

    }
    
    public ForumThread getThread() {
        return thread;
    }
    
    public void reset() throws IOException {
        thread.reset();
    }
}
