package werewolf.net;

import java.io.IOException;

public interface ThreadManager
{
	public ForumThread getThread();

	public void reset() throws IOException;

	public boolean update() throws IOException;
}
