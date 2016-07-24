package werewolf.net;

import java.io.IOException;
import java.util.List;

public interface GameRecord extends ThreadManager
{
	public void addGame(ForumThread thread, List<? extends ForumUser> hosts, List<? extends ForumUser> winners, List<? extends ForumUser> losers) throws IOException;

	public void initalize() throws IOException;

	public void save() throws IOException;
}