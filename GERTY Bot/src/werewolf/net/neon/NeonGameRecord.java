package werewolf.net.neon;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;

import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import werewolf.net.ForumPostEditor;
import werewolf.net.ForumThread;
import werewolf.net.ForumUser;
import werewolf.net.GameRecord;
import werewolf.net.Utils;


public class NeonGameRecord implements GameRecord {
    @SuppressWarnings("compatibility:-4990742336106630756")
    private static final long serialVersionUID = -6411468909702922911L;

    private boolean                        initalized = false;
    private boolean                        initalizing = false;
    private static final int               threadId = 16851;
    private static final int               boardId = 179;
    private static final String            gameIdentifier = "Games to Date";
    private static LinkedList<WinLossUser> users = new LinkedList<WinLossUser>();
    private static LinkedList<WinLossGame> games = new LinkedList<WinLossGame>();
    private static int                     requiredActivityCount = 2;

    public static final NeonGameRecord INSTANCE = new NeonGameRecord();



    private static class WinLossUser extends ForumUser implements Comparable<WinLossUser> {
        @SuppressWarnings("compatibility:-6442071880195099061")
        private static final long serialVersionUID = 8435231982556605921L;

        protected int   wins = 0;
        protected int   losses = 0;
        protected int   hosts = 0;
        protected int[] inactivityCount = new int[] { 0, 0, 0 };

        public WinLossUser(int u, String name) {
            super(u, name, NeonContext.INSTANCE);
            ForumUser.getUserFor(u, name, NeonContext.INSTANCE);
        }

        public WinLossUser(ForumUser user) {
            super(user);
        }

        public double getWinRatio() {
            return getWinCount() / (double)getGameCount();
        }

        public double getRankRatio() {
            return getWinRatio() - .1 * Math.max(0, getActivityCount() - requiredActivityCount);
        }

        public int getWinPercent() {
            return (int)Math.round(getWinRatio() * 100);
        }

        public int getWinCount() {
            return wins + hosts; //Hosting counts as a win.
        }

        public int getGameCount() {
            return wins + losses + hosts;
        }

        public int getActivityCount() {
            return Math.min(Math.min(inactivityCount[0], inactivityCount[1]), inactivityCount[2]);
        }

        public boolean isActive() {
            return getActivityCount() < requiredActivityCount;
        }

        @Override
        public List<String> getAliases() {
            return ForumUser.getUserFor(super.getUserId(), super.getName(), NeonContext.INSTANCE).getAliases();
        }

        @Override
        public boolean addAlias(String alias) {
            return ForumUser.getUserFor(super.getUserId(), super.getName(), NeonContext.INSTANCE).addAlias(alias);
        }

        @Override
        public String toString() {
            return super.getName();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof WinLossUser))
                return false;

            ForumUser thisUser = ForumUser.getUserFor(super.getUserId(), super.getName(), NeonContext.INSTANCE);
            ForumUser otherUser =
                ForumUser.getUserFor(((ForumUser)o).getUserId(), ((ForumUser)o).getName(), NeonContext.INSTANCE);
            return thisUser.equals(otherUser);
        }

        @Override
        public int compareTo(NeonGameRecord.WinLossUser o) {
            int ratio = (int)Math.signum(o.getRankRatio() - getRankRatio());
            if (ratio != 0)
                return ratio;
            
            ratio = (int)Math.signum(o.getWinRatio() - getWinRatio());
            if (ratio != 0)
                return ratio;
            
            if (getWinRatio() >= .5)
                return o.getGameCount() - getGameCount();
            return getGameCount() - o.getGameCount();
        }
    }

    private static class WinLossGame implements Comparable<WinLossGame> {
        public static final int UNKNOWN_GAME = 0;
        public static final int WEREWOLF_GAME = 1;
        public static final int ASSASSINS_GAME = 2;
        public static final int MAFIA_GAME = 3;

        public final int                     type;
        public final String                  threadId;
        public final String                  title;
        public final int                     number;
        public final LinkedList<WinLossUser> hosts;
        public final LinkedList<WinLossUser> winners;
        public final LinkedList<WinLossUser> losers;

        public WinLossGame(String name, String threadId, LinkedList<? extends ForumUser> hosts,
                           LinkedList<? extends ForumUser> winners, LinkedList<? extends ForumUser> losers) {
            switch (name.replaceFirst(" .*", "").toLowerCase()) {
                case "were":
                case "ww":
                    type = WEREWOLF_GAME;
                    break;
                case "witch":
                case "assassins":
                    type = ASSASSINS_GAME;
                    break;
                case "mafia":
                    type = MAFIA_GAME;
                    break;
                default:
                    type = UNKNOWN_GAME;
            }
            this.title = name;
            number = getGameNumber();
            this.threadId = threadId;
            this.hosts = INSTANCE.userConvert(hosts);
            this.winners = INSTANCE.userConvert(winners);
            this.losers = INSTANCE.userConvert(losers);
        }

        public WinLossGame(String name, String threadId) {
            switch (name.replaceFirst(" .*", "").toLowerCase()) {
                case "were":
                case "ww":
                    type = WEREWOLF_GAME;
                    break;
                case "assassins":
                case "witch":
                    type = ASSASSINS_GAME;
                    break;
                case "mafia":
                    type = MAFIA_GAME;
                    break;
                default:
                    type = UNKNOWN_GAME;
            }
            title = name;
            number = getGameNumber();
            this.threadId = threadId;
            hosts = new LinkedList<WinLossUser>();
            winners = new LinkedList<WinLossUser>();
            losers = new LinkedList<WinLossUser>();
        }


        private int getGameNumber() {
            String numeral = title.replaceAll(":.*", "").replaceAll(".* ", "");
            return Utils.RomanNumerals.decode(numeral);
        }


        @Override
        public int compareTo(WinLossGame o) {
            return number - o.number;
        }

        @Override
        public String toString() {
            StringBuilder output = new StringBuilder();

            output.append(title + " (id=" + threadId + ")\nHosts: ");
            String name = "";
            for (WinLossUser host : hosts) {
                if (name.length() > 0)
                    output.append(name + ", ");
                name = host.getName();
            }
            output.append(name + "\nWinners: ");
            name = "";


            for (WinLossUser player : winners) {
                if (name.length() > 0)
                    output.append(name + ", ");
                name = player.getName();
            }
            output.append(name + "\nLosers: ");
            name = "";


            for (WinLossUser player : losers) {
                if (name.length() > 0)
                    output.append(name + ", ");
                name = player.getName();
            }
            output.append(name);

            return output.toString();
        }
    }


    private NeonGameRecord() {}


    @Override
    public void save() throws IOException {
        initalize();
        int inactiveSeniorPlayers = 0;
        int inactiveJuniorPlayers = 0;

        LinkedList<WinLossUser> seniorPlayers = new LinkedList<WinLossUser>();
        LinkedList<WinLossUser> juniorPlayers = new LinkedList<WinLossUser>();

        LinkedList<WinLossGame> werewolfGames = new LinkedList<WinLossGame>();
        LinkedList<WinLossGame> assassinsGames = new LinkedList<WinLossGame>();
        LinkedList<WinLossGame> mafiaGames = new LinkedList<WinLossGame>();

        for (WinLossGame game : games) {
            switch (game.type) {
                case WinLossGame.MAFIA_GAME:
                    mafiaGames.add(game);
                    break;
                case WinLossGame.WEREWOLF_GAME:
                    werewolfGames.add(game);
                    break;
                case WinLossGame.ASSASSINS_GAME:
                    assassinsGames.add(game);
                    break;
                default:
                    werewolfGames.add(game);
            }
        }
        Collections.sort(werewolfGames);
        Collections.sort(assassinsGames);
        Collections.sort(mafiaGames);


        for (WinLossGame game : werewolfGames) {
            for (WinLossUser user : users)
                user.inactivityCount[0]++;
            for (WinLossUser user : game.winners)
                user.inactivityCount[0] = 0;
            for (WinLossUser user : game.losers)
                user.inactivityCount[0] = 0;
            for (WinLossUser user : game.hosts)
                user.inactivityCount[0] = 0;
        }
        for (WinLossGame game : assassinsGames) {
            for (WinLossUser user : users)
                user.inactivityCount[1]++;
            for (WinLossUser user : game.winners)
                user.inactivityCount[1] = 0;
            for (WinLossUser user : game.losers)
                user.inactivityCount[1] = 0;
            for (WinLossUser user : game.hosts)
                user.inactivityCount[1] = 0;
        }
        for (WinLossGame game : mafiaGames) {
            for (WinLossUser user : users)
                user.inactivityCount[2]++;
            for (WinLossUser user : game.winners)
                user.inactivityCount[2] = 0;
            for (WinLossUser user : game.losers)
                user.inactivityCount[2] = 0;
            for (WinLossUser user : game.hosts)
                user.inactivityCount[2] = 0;
        }

        for (WinLossUser user : users) {
            if (user.getGameCount() >= 5) {
                seniorPlayers.add(user);
                if (!user.isActive())
                    inactiveSeniorPlayers++;
            } else {
                juniorPlayers.add(user);
                if (!user.isActive())
                    inactiveJuniorPlayers++;
            }
        }
        Collections.sort(seniorPlayers);
        Collections.sort(juniorPlayers);

        ForumPostEditor editor = NeonThread.getThread(boardId, threadId).getPosts().get(0).getEditor();
        editor.setText("This is a record of results of all official games. This record currently tracks " +
                       games.size() + " official games and " + users.size() +
                       " official players. Survival is not usually necessary to count as a win, hosting will always " +
                       "count as a win, and modkills will always count as a loss. The inactivity counter below counts " +
                       "how many games have passed in ALL queues since the player last played. For the purpose of " +
                       "ranking, players will lose 10% of their win ratio for each game beyond the maximum of " +
                       requiredActivityCount + " allowed in their counter. This will only affect the order players " +
                       "are displayed - not the displayed win percentage." +
                       "\n\nTo have GERTY automatically update this thread, hosts may use the [end] command.\n" +
                       "[b]Syntax:[/b] [end winner1, winner2, etc]");

        editor.appendText("\n\n\n[size=150][u][b]Player Rankings (" + seniorPlayers.size() + " Senior Players - " +
                          inactiveSeniorPlayers +
                          " Inactive)[/b][/u][/size]\nWon/Total/Hosted/Inactivity Counter (Win%)\n");
        for (WinLossUser user : seniorPlayers)
            saveUser(user, editor);


        editor.appendText("\n\n\nPlayers with less than five completed games (" + juniorPlayers.size() +
                          " Junior Players - " + inactiveJuniorPlayers + " Inactive):\n[spoiler]");
        for (WinLossUser user : juniorPlayers)
            saveUser(user, editor);

        editor.appendText("[/spoiler]\n\n\n\n[size=150][b]Games to Date[/b][/size]\n[hr][/hr]");

        for (WinLossGame game : werewolfGames) {
            editor.appendText("\n[b][url=" + NeonContext.INSTANCE.getThreadUrl("0", game.threadId + "") + "]" +
                              game.title + "[/url][/b]\n" + "Hosts: " + StringUtils.join(game.hosts, ", ") + "\n" +
                              "Winners: " + StringUtils.join(game.winners, ", ") + "\n" + "Losers: " +
                              StringUtils.join(game.losers, ", ") + "\n\n");
        }
        editor.appendText("[hr][/hr]");

        for (WinLossGame game : assassinsGames) {
            editor.appendText("\n[b][url=" + NeonContext.INSTANCE.getThreadUrl("0", game.threadId + "") + "]" +
                              game.title + "[/url][/b]\n" + "Hosts: " + StringUtils.join(game.hosts, ", ") + "\n" +
                              "Winners: " + StringUtils.join(game.winners, ", ") + "\n" + "Losers: " +
                              StringUtils.join(game.losers, ", ") + "\n\n");
        }
        editor.appendText("[hr][/hr]");

        for (WinLossGame game : mafiaGames) {
            editor.appendText("\n[b][url=" + NeonContext.INSTANCE.getThreadUrl("0", game.threadId + "") + "]" +
                              game.title + "[/url][/b]\n" + "Hosts: " + StringUtils.join(game.hosts, ", ") + "\n" +
                              "Winners: " + StringUtils.join(game.winners, ", ") + "\n" + "Losers: " +
                              StringUtils.join(game.losers, ", ") + "\n\n");
        }

        editor.submit();
    }


    private void saveUser(WinLossUser user, ForumPostEditor editor) throws IOException {
        if (!user.isActive())
            editor.appendText("[color=#6090A0]");
        editor.appendText(user.getWinCount() + "/" + user.getGameCount() + "/" + user.hosts + "/" +
                          user.getActivityCount() + " (" + user.getWinPercent() + "%)");
        if (!user.isActive())
            editor.appendText("[/color]");
        editor.appendText(" - [url=" + user.getProfileUrl() + "]" + user.getName() + "[/url]");


        List<String> aliases = user.getAliases();
        if (aliases.size() > 0) {
            editor.appendText("  (Aliases: ");
            String lastAlias = "";
            for (String alias : aliases) {
                if (lastAlias.length() > 0)
                    editor.appendText("[url=" + user.getProfileUrl() + "]" + lastAlias + "[/url], ");
                lastAlias = alias;
            }
            editor.appendText("[url=" + user.getProfileUrl() + "]" + lastAlias + "[/url])");
        }
        editor.appendText("\n");
    }


    @Override
    public void initalize() throws IOException {
        if (initalizing)
            throw new IOException("Cannot modify while initalizing.");
        if (!initalized) {
            initalizing = true;
            HtmlPage page = NeonContext.INSTANCE.getThreadPage("0", threadId + "");
            List<?>  spoilerLinks = page.getByXPath("//a[text()='show']");
            for (int i = 0; i < spoilerLinks.size(); ++i) {
                try {
                    HtmlAnchor link = (HtmlAnchor)spoilerLinks.get(i);
                    link.click();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            String post =
                "//div[@id='pagecontent']/table[@class='tablebg'][last()]/tbody/tr[2]/td//div[@class='postbody']";
            HtmlDivision               postElement = (HtmlDivision)page.getByXPath(post).get(0);
            Hashtable<String, Integer> gameTable = new Hashtable<String, Integer>();
            List<?>                    playerLinks = postElement.getByXPath(post + "/a|" + post + "/div/div/div/a");
            List<?>                    gameLinks = postElement.getByXPath(post + "/strong/a");
            System.out.println("Games Found: " + gameLinks.size() + ", Players Found: " + playerLinks.size());

            for (Object obj : playerLinks) {
                HtmlAnchor playerLink = (HtmlAnchor)obj;
                String     url = playerLink.getAttribute("href");
                if (url.endsWith("#")) //URLs for spoiler links.
                    continue;
                int id = -1;
                if (url.lastIndexOf("&u=") > 0)
                    id = Integer.parseInt(url.substring(url.lastIndexOf("&u=") + 3));
                WinLossUser user = new WinLossUser(id, playerLink.asText());
                if (!users.contains(user))
                    users.add(new WinLossUser(id, playerLink.asText()));
            }

            for (Object obj : gameLinks) {
                HtmlAnchor gameLink = (HtmlAnchor)obj;
                String     url = gameLink.getAttribute("href");
                if (url.endsWith("#")) //URLs for spoiler links.
                    continue;
                int id = -1;
                int index = url.lastIndexOf("t=");
                if (index > 0 && index < url.length() - 2)
                    id = Integer.parseInt(url.substring(url.lastIndexOf("t=") + 2));

                gameTable.put(gameLink.asText(), id);
            }

            String gameText = postElement.asText();
            gameText = gameText.substring(gameText.indexOf(gameIdentifier) + gameIdentifier.length()).trim();
            String[] rawGames = gameText.split("\\s\\s\\s+");
            for (String rawGame : rawGames) {
                String[] rawGameData = rawGame.split("\n[^\\s]+\\: ?");
                String   title = rawGameData[0].trim();
                String[] hosts = rawGameData[1].trim().split("\\s*,\\s*");
                String[] winners = rawGameData[2].trim().split("\\s*,\\s*");
                String[] losers = rawGameData[3].trim().split("\\s*,\\s*");

                WinLossGame game = new WinLossGame(title, gameTable.get(title).toString());

                for (String host : hosts) {
                    WinLossUser user = getUser(host.trim());
                    game.hosts.add(user);
                    user.hosts++;
                }
                for (String winner : winners) {
                    WinLossUser user = getUser(winner.trim());
                    game.winners.add(user);
                    user.wins++;
                }
                for (String loser : losers) {
                    WinLossUser user = getUser(loser.trim());
                    game.losers.add(user);
                    user.losses++;
                }

                games.add(game);
            }
        }
        initalizing = false;
        initalized = true;
    }

    public WinLossUser getUser(ForumUser player) {
        for (WinLossUser user : users) {
            if (user.getUserId() > 0) {
                if (user.equals(player)) {
                    if (!user.getName().equals(player.getName()))
                        user.addAlias(player.getName());
                    return user;
                }
            } else if (player.getName().equalsIgnoreCase(user.getName())) {
                user.setUserId(player.getUserId());
                return user;
            }
        }
        WinLossUser user = new WinLossUser(player);
        users.add(user);
        return user;
    }

    private WinLossUser getUser(String name) {
        for (WinLossUser user : users) {
            if (user.getName().equalsIgnoreCase(name)) {
                if (user.getUserId() <= 0)
                    System.err.println("Unloaded player id: " + user.getName());
                return user;
            }
        }
        ForumUser forumUser = ForumUser.getUserFor(name, NeonContext.INSTANCE);
        if (forumUser == null)
            throw new IllegalArgumentException("Unknown player: " + name);
        WinLossUser user = new WinLossUser(forumUser);
        users.add(user);
        return user;
    }


    public void addGame(ForumThread thread, LinkedList<? extends ForumUser> hosts,
                        LinkedList<? extends ForumUser> winners,
                        LinkedList<? extends ForumUser> losers) throws IOException {
        initalize();
        String threadName = thread.getTitle();

        if (threadName.contains("["))
            threadName = threadName.substring(0, threadName.indexOf("["));
        if (threadName.contains("|"))
            threadName = threadName.substring(0, threadName.indexOf("|"));
        if (threadName.contains("("))
            threadName = threadName.substring(0, threadName.indexOf("("));
        threadName = threadName.trim();

        for (WinLossGame game : games) {
            if (game.title.equalsIgnoreCase(threadName))
                return;
        }

        WinLossGame game = new WinLossGame(threadName, thread.getThreadId(), hosts, winners, losers);
        games.add(game);
        for (WinLossUser host : game.hosts)
            host.hosts++;
        for (WinLossUser player : game.winners)
            player.wins++;
        for (WinLossUser player : game.losers)
            player.losses++;
        save();
        System.out.println("Game saved: " + threadName + "\n" + game);
    }

    private LinkedList<WinLossUser> userConvert(LinkedList<? extends ForumUser> list) {
        LinkedList<WinLossUser> output = new LinkedList<WinLossUser>();
        for (ForumUser user : list) {
            if (user != null)
                output.add(getUser(user));
        }
        return output;
    }

    @Override
    public ForumThread getThread() {
        return null; // new NeonThread(179, 16851);
    }

    @Override
    public void reset() throws IOException {
        
    }

    @Override
    public boolean update() throws IOException {
        return false;
    }


    public static void main(String... cheese) {
        try {
            INSTANCE.initalize();
            INSTANCE.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
