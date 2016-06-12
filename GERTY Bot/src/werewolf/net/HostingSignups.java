package werewolf.net;

import java.io.IOException;
import java.io.Serializable;

public interface HostingSignups extends ThreadManager {
    public void endGame(String threadId) throws IOException;
    
    public void checkThreads()throws IOException;
}
