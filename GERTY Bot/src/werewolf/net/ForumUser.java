package werewolf.net;

import java.io.IOException;

import java.util.Iterator;
import java.util.LinkedList;

import java.util.List;

import werewolf.game.User;

/**
 * This class describes a user on a forum, and the actions the bot may take to interact with or get information from
 * that user.
 */
public class ForumUser implements User {
    @SuppressWarnings("compatibility:-1848784792197264369")
    private static final long serialVersionUID = -4838253248167596821L;

    private int                userId;
    private ForumContext       context;
    private String             name;
    private LinkedList<String> aliases = new LinkedList<String>();


    /**
     * Creates a new ForumUser with the specified data.
     *
     * @param u The user id for this ForumUser. This is the unique identity for the user.
     * @param name The name of this ForumUser as displayed on the forum. This field may change.
     * @param context
     */
    protected ForumUser(int u, String name, ForumContext context) {
        userId = u;
        this.name = name;
        this.context = context;
    }

    /**
     * Uses the data from the given ForumUser to create a new user with the same data.
     *
     * @param oth The ForumUser to clone.
     */
    protected ForumUser(ForumUser oth) {
        userId = oth.getUserId();
        name = oth.getName();
        context = oth.getContext();
    }

    /**
     *  Sets up and sends a new PM to this user.
     *
     * @param body The text for the body of the message.
     * @param subject The subject of the message.
     * @throws IOException If the underlying network calls fail for any reason.
     */
    public void replyTo(String body, String subject) throws IOException {
        getContext().makePm(new String[] { name }, new String[0], body, subject);
    }

    /**
     * @return The user's current name.
     */
    @Override
    public String getName() {
        return name;
    }
    
    
    public void setName(String newName) {
        if (newName.equalsIgnoreCase(name))
            return;
        Iterator<String> iter = aliases.iterator();
        while (iter.hasNext()) {
            if (iter.next().equalsIgnoreCase(newName))
                iter.remove();
        }
        addAlias(name);
        name = newName;
    }
    

    /**
     * @return This user's unique user id.
     */
    public int getUserId() {
        return userId;
    }

    /**
     * @return A url to this user's profile page.
     */
    public String getProfileUrl() {
        return getContext().getUserProfileUrl(userId);
    }

    /**
     * Sets this user's user id. Throws an IllegalArgumentException if the user already has an id.
     *
     * @param newId The id to set.
     */
    public void setUserId(int newId) {
        if (userId > 0)
            throw new IllegalArgumentException("User already has a valid id.");
        userId = newId;
        context.USERS.userIdSet(this);
    }


    /**
     * Adds a new alias for a user. Usually only called when a user changes their name.
     *
     * @param alias The alias of the user.
     */
    public boolean addAlias(String alias) {
        if (alias.equalsIgnoreCase(name))
            return false;
        for (String aliasCheck : aliases) {
            if (aliasCheck.equalsIgnoreCase(alias))
                return false;
        }
        aliases.add(alias);
        
        try {
            context.RECORD.save();
        } catch (IOException e) {
            if (!e.getMessage().contains("Cannot modify while initalizing"))
                e.printStackTrace();
        }
        catch (NullPointerException ex) {
            return true;
        }
        return true;
    }


    /**
     * Returns the list of aliases the user is known to have.
     *
     * @return The list of aliases the user is known to have.
     */
    public List<String> getAliases() {
        return aliases;
    }


    /**
     * @return The context (usually website-based) this user exists in.
     */
    public ForumContext getContext() {
        return context;
    }


    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Returns true if this use has the same user id as the given user.
     * @param o The ForumUser to compare to.
     * @return
     */
    public boolean equals(ForumUser o) {
        if (o == null)
            return false;
        return o.getUserId() == getUserId() && o.getContext().equals(getContext());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ForumUser)
            return equals((ForumUser)o);
        return false;
    }


    @Override
    public String toString() {
        return name + "(id=" + userId + ", context=" + context + ")";
    }


    public static ForumUser getUserFor(int u, ForumContext context) {        
        return context.USERS.getUser(u);
    }


    public static ForumUser getUserFor(int u, String name, ForumContext context) {
        ForumUser user = getUserFor(u, context);
        if (user != null) {
            user.addAlias(name);
            return user;
        }

        user = getUserFor(name, context);
        if (user != null) {
            if (user.getUserId() <= 0)
                user.setUserId(u);
            else if (u != user.getUserId())
                throw new IllegalArgumentException("ForumUser id mismatch: " + user);
            user.addAlias(name);
            return user;
        }

        user = new ForumUser(u, name, context);
        user.addAlias(name);
        context.USERS.addUser(user);
        return user;
    }


    public static ForumUser getUserFor(String name, ForumContext context) {        
        ForumUser user = context.USERS.getUser(name);
        if (user == null) {
            user = new ForumUser(0, name, context);
            context.USERS.addUser(user);
        }
        return user;
    }
}
