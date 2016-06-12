package werewolf.net;

import java.io.IOException;
import java.io.Serializable;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

public class ForumUserDatabase implements Serializable {
    private Hashtable<Integer, ForumUser> users = new Hashtable<Integer, ForumUser>();
    private LinkedList<ForumUser> unknownUsers = new LinkedList<ForumUser>();
    public final ForumContext context;
    
    private boolean initalized = false;

    public ForumUserDatabase(ForumContext context) {
        this.context = context;
    }
    
    public void addUser(ForumUser user) {
        if (getUser(user.getName()) != null)
            return;

        if (user.getUserId() > 0)
            users.put(user.getUserId(), user);
        unknownUsers.add(user);
    }
    
    public ForumUser getUser(Integer u, String name) {
        ForumUser user = getUser(u);
        if (user != null)
            return user;
        return getUser(name);
    }
    
    public ForumUser getUser(Integer u) {
        return users.get(u);
    }
    
    public void userIdSet(ForumUser user) {
        //If valid id is present and user was previously known, log.
        if (user.getUserId() > 0 && unknownUsers.remove(user))
            users.put(user.getUserId(), user);
    }
    
    public ForumUser getUser(String name) {
        boolean multipleAliases = false;
        ForumUser found = null;
        Enumeration<ForumUser> elements = users.elements();
        while (elements.hasMoreElements()) {
            ForumUser user = elements.nextElement();
            if (user.getName().equalsIgnoreCase(name))
                return user;
            for (String alias : user.getAliases()) {
                if (alias.equalsIgnoreCase(name)) {
                    if (found == null)
                        found = user;
                    else
                        multipleAliases = true;
                }
            }
        }
        if (!multipleAliases)
            return found;
        return null;
    }
    
    public Collection<ForumUser> getKnownUsers() {
        return users.values();
    }
}
