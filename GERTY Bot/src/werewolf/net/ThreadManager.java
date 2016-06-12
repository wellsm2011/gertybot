package werewolf.net;

import java.io.IOException;

public interface ThreadManager {
    public boolean update() throws IOException;
    
    public void reset() throws IOException;
    
    public ForumThread getThread();
}
