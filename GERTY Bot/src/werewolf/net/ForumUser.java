package werewolf.net;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import werewolf.game.User;

/**
 * This class describes a user on a forum, and the actions the bot may take to
 * interact with or get information from that user.
 */
public class ForumUser implements User
{
	private static final long	serialVersionUID	= -4838253248167596821L;

	public static ForumUser getUserFor(int u, ForumContext context)
	{
		return context.USERS.getUser(u);
	}

	public static ForumUser getUserFor(int u, String name, ForumContext context)
	{
		ForumUser user = ForumUser.getUserFor(u, context);
		if (user != null)
		{
			user.addAlias(name);
			return user;
		}

		user = ForumUser.getUserFor(name, context);
		if (user != null)
		{
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

	public static ForumUser getUserFor(String name, ForumContext context)
	{
		ForumUser user = context.USERS.getUser(name);
		if (user == null)
		{
			user = new ForumUser(0, name, context);
			context.USERS.addUser(user);
		}
		return user;
	}

	private int					userId;
	private ForumContext		context;
	private String				name;
	private LinkedList<String>	aliases	= new LinkedList<String>();

	/**
	 * Uses the data from the given ForumUser to create a new user with the same
	 * data.
	 *
	 * @param oth
	 *            The ForumUser to clone.
	 */
	protected ForumUser(ForumUser oth)
	{
		this.userId = oth.getUserId();
		this.name = oth.getName();
		this.context = oth.getContext();
	}

	/**
	 * Creates a new ForumUser with the specified data.
	 *
	 * @param u
	 *            The user id for this ForumUser. This is the unique identity
	 *            for the user.
	 * @param name
	 *            The name of this ForumUser as displayed on the forum. This
	 *            field may change.
	 * @param context
	 */
	protected ForumUser(int u, String name, ForumContext context)
	{
		this.userId = u;
		this.name = name;
		this.context = context;
	}

	/**
	 * Adds a new alias for a user. Usually only called when a user changes
	 * their name.
	 *
	 * @param alias
	 *            The alias of the user.
	 */
	public boolean addAlias(String alias)
	{
		if (alias.equalsIgnoreCase(this.name))
			return false;
		for (String aliasCheck : this.aliases)
			if (aliasCheck.equalsIgnoreCase(alias))
				return false;
		this.aliases.add(alias);

		try
		{
			this.context.RECORD.save();
		} catch (IOException e)
		{
			if (!e.getMessage().contains("Cannot modify while initalizing"))
				e.printStackTrace();
		} catch (NullPointerException ex)
		{
			return true;
		}
		return true;
	}

	/**
	 * Returns true if this use has the same user id as the given user.
	 * 
	 * @param o
	 *            The ForumUser to compare to.
	 * @return
	 */
	public boolean equals(ForumUser o)
	{
		if (o == null)
			return false;
		return o.getUserId() == this.getUserId() && o.getContext().equals(this.getContext());
	}

	@Override
	public boolean equals(Object o)
	{
		if (o instanceof ForumUser)
			return this.equals((ForumUser) o);
		return false;
	}

	/**
	 * Returns the list of aliases the user is known to have.
	 *
	 * @return The list of aliases the user is known to have.
	 */
	public List<String> getAliases()
	{
		return this.aliases;
	}

	/**
	 * @return The context (usually website-based) this user exists in.
	 */
	public ForumContext getContext()
	{
		return this.context;
	}

	/**
	 * @return The user's current name.
	 */
	@Override
	public String getName()
	{
		return this.name;
	}

	/**
	 * @return A url to this user's profile page.
	 */
	public String getProfileUrl()
	{
		return this.getContext().getUserProfileUrl(this.userId);
	}

	/**
	 * @return This user's unique user id.
	 */
	public int getUserId()
	{
		return this.userId;
	}

	@Override
	public int hashCode()
	{
		return this.name.hashCode();
	}

	/**
	 * Sets up and sends a new PM to this user.
	 *
	 * @param body
	 *            The text for the body of the message.
	 * @param subject
	 *            The subject of the message.
	 * @throws IOException
	 *             If the underlying network calls fail for any reason.
	 */
	public void replyTo(String body, String subject) throws IOException
	{
		this.getContext().makePm(new String[]
		{ this.name }, new String[0], body, subject);
	}

	public void setName(String newName)
	{
		if (newName.equalsIgnoreCase(this.name))
			return;
		Iterator<String> iter = this.aliases.iterator();
		while (iter.hasNext())
			if (iter.next().equalsIgnoreCase(newName))
				iter.remove();
		this.addAlias(this.name);
		this.name = newName;
	}

	/**
	 * Sets this user's user id. Throws an IllegalArgumentException if the user
	 * already has an id.
	 *
	 * @param newId
	 *            The id to set.
	 */
	public void setUserId(int newId)
	{
		if (this.userId > 0)
			throw new IllegalArgumentException("User already has a valid id.");
		this.userId = newId;
		this.context.USERS.userIdSet(this);
	}

	@Override
	public String toString()
	{
		return this.name + "(id=" + this.userId + ", context=" + this.context + ")";
	}
}
