package werewolf.net;

import java.io.IOException;

public interface HostingSignups extends ThreadManager
{
	public void checkThreads() throws IOException;

	public void endGame(String threadId) throws IOException;
}
