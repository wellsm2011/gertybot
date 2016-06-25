package werewolf.net;

import java.io.IOException;
import java.util.LinkedList;

public interface GameRecord extends ThreadManager
{
	public void addGame(ForumThread thread, LinkedList<? extends ForumUser> hosts, LinkedList<? extends ForumUser> winners, LinkedList<? extends ForumUser> losers) throws IOException;

	public void initalize() throws IOException;

	public void save() throws IOException;
}