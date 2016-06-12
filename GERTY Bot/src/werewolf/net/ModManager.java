package werewolf.net;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;

import java.util.LinkedList;

import werewolf.net.neon.NeonContext;


//TODO: Complete class.
public class ModManager {
    public static final int groupId = 27;

    private static LinkedList<ForumUser> permamods = new LinkedList<ForumUser>();
    private static LinkedList<ForumUser> mods = new LinkedList<ForumUser>();
    private static boolean               initalized = false;


    private static void initalize() throws IOException {
        HtmlPage page =
            NeonContext.INSTANCE.getPage(NeonContext.DOMAIN + "ucp.php?i=groups&mode=manage&action=list&g=" + groupId);
    }

    public static void addMods(LinkedList<ForumUser> mods) {

    }

    public static void removeMods(LinkedList<ForumUser> mods) {

    }

    public static void setMods(LinkedList<ForumUser> users) throws IOException {
        if (!initalized)
            initalize();
        LinkedList<ForumUser> remove = (LinkedList<ForumUser>)mods.clone();
        LinkedList<ForumUser> add = (LinkedList<ForumUser>)users.clone();
        remove.removeAll(users); // Only remove players who are currently mods, but not in the new mod list.
        add.removeAll(mods); // Only add players who are in the add list, but not the mod list or the permamod list.
        add.removeAll(permamods);

        removeMods(remove);
        addMods(add);
    }
}
