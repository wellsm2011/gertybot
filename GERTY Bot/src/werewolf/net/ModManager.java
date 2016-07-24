package werewolf.net;

import java.io.IOException;
import java.util.LinkedList;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import werewolf.net.neon.NeonContext;

//TODO: Complete class.
public class ModManager
{
	public static final int groupId = 27;

	private static LinkedList<ForumUser>	permamods	= new LinkedList<>();
	private static LinkedList<ForumUser>	mods		= new LinkedList<>();
	private static boolean					initalized	= false;

	public static void addMods(LinkedList<ForumUser> mods)
	{

	}

	private static void initalize() throws IOException
	{
		HtmlPage page = NeonContext.INSTANCE.getPage(NeonContext.DOMAIN + "ucp.php?i=groups&mode=manage&action=list&g=" + ModManager.groupId);
	}

	public static void removeMods(LinkedList<ForumUser> mods)
	{

	}

	public static void setMods(LinkedList<ForumUser> users) throws IOException
	{
		if (!ModManager.initalized)
			ModManager.initalize();
		LinkedList<ForumUser> remove = (LinkedList<ForumUser>) ModManager.mods.clone();
		LinkedList<ForumUser> add = (LinkedList<ForumUser>) users.clone();
		remove.removeAll(users);
		/*
		 * Only remove players who are currently mods, but not in the new mod
		 * list.
		 */
		add.removeAll(ModManager.mods);
		/*
		 * Only add players who are in the add list, but not the mod list or the
		 * permamod list.
		 */
		add.removeAll(ModManager.permamods);

		ModManager.removeMods(remove);
		ModManager.addMods(add);
	}
}
