package werewolf.net.halolz;

import java.io.IOException;

import java.util.LinkedList;

import werewolf.net.ForumThread;
import werewolf.net.ForumUser;
import werewolf.net.GameRecord;
import werewolf.net.neon.NeonGameRecord;

public class HalolzGameRecord implements GameRecord {
    @SuppressWarnings("compatibility:7983391852231369103")
    private static final long serialVersionUID = 5660545790993107497L;

    public static final HalolzGameRecord INSTANCE = new HalolzGameRecord();

    private HalolzGameRecord() {}

    @Override
    public void addGame(ForumThread thread, LinkedList<? extends ForumUser> hosts,
                        LinkedList<? extends ForumUser> winners,
                        LinkedList<? extends ForumUser> losers) throws IOException {}


    @Override
    public void save() {}


    @Override
    public void initalize() {}
    
    @Override
    public ForumThread getThread() {
        return null;
    }
    
    @Override
    public void reset() throws IOException {
        
    }
    
    @Override
    public boolean update() throws IOException {
        return false;
    }
}
